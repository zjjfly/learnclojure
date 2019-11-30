(ns learnclojure.ch11.optimize
  (:import (clojure.lang IFn$DL IFn)))

;在计算密集型的程序中,数字运算的效率至关重要.clojure程序可以通过如下的方法提高效率:
;1.使用原始类型,原始类型的大多数操作都是在硬件层面实现的,效率非常高,封装类型的运算效率和原始类型是差一个数量级的
;2.避免使用集合和序列.这是因为JVM的集合和序列的元素必须是对象,不能是原始类型.这就违背了第一条.所以采用数组是更好的选择
;下面是具体的是实践

;声明函数接受和返回原始类型
;clojure中的函数都实现了IFn接口,它定义了很多invoke方法,它们的参数和返回的类型都是Object,这导致即使传入的数字都会自动的转换成相应的封装类型
;之前提到clojure可以加上类型提示来避免使用反射,但类型提示不会改变这些方法的函数签名.但clojure也提供静态声明函数的参数和返回值是原始类型,具体是指double和long
;看一个一般的clojure函数
(defn foo [a] 0)
(seq (.getDeclaredMethods (class foo)))
;(#object[java.lang.reflect.Method
;         0x3ddda6fc
;         "public java.lang.Object learnclojure.ch11.optimize$foo.invoke()"]
; #object[java.lang.reflect.Method
;         0x66e56c1e
;         "public static java.lang.Object learnclojure.ch11.optimize$foo.invokeStatic()"])
;可以看到参数和返回值类型都是Object.加类型提示再试试
(defn foo [^Double a] 0)
(seq (.getDeclaredMethods (class foo)))
;(#object[java.lang.reflect.Method
;         0x3ddda6fc
;         "public java.lang.Object learnclojure.ch11.optimize$foo.invoke()"]
; #object[java.lang.reflect.Method
;         0x66e56c1e
;         "public static java.lang.Object learnclojure.ch11.optimize$foo.invokeStatic()"])
;即使Double在语义上是一个数字,但它仍然是一个类,因而参数类型是仍是Object
;接下来试试原始类型的声明
(defn round ^long [^double a] (Math/round a))
(seq (.getDeclaredMethods (class round)))
;(#object[java.lang.reflect.Method
;         0x53b004e3
;         "public java.lang.Object learnclojure.ch11.optimize$round.invoke(java.lang.Object)"]
; #object[java.lang.reflect.Method
;         0x1dd6b266
;         "public static long learnclojure.ch11.optimize$round.invokeStatic(double)"]
; #object[java.lang.reflect.Method
;         0x11e72ffd
;         "public final long learnclojure.ch11.optimize$round.invokePrim(double)"])
;现在多了一个方法,它的参数和返回值都是原始类型
;实际上,是foo这个对象实现了另一个在IFn中定义的接口DL
(instance? IFn round)
;true
(instance? IFn$DL round)
;true
;它不能接受一个类型不相符的参数.
(try
  (round "string")
  (catch ClassCastException e
    (.getMessage e)))
;java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number
;可以看出这个方法可以接受Number类型的参数,只要参数在预期的范围内
(round 1.3M)
;1
(round 3/2)
;2
;还可以看出,之前的参数和返回值类型是Object的方法仍在,这是为了能继续和高阶函数一起使用
(map round [4.5 6.1 3])
;(5 6 3)
(apply round [1.2])
;1

;类型声明和提示都可以用在deftype和defrecord类型的字段说明(因而也包括构造函数)
;但有原始类型声明有一个限制:参数个数最多是4个
;声明参数和返回值为原始类型也可能导致明显不一致的情景
;(defn foo ^long [^int a] 0)
;java.lang.IllegalArgumentException: Only long and double primitives are supported
;(defn foo ^long [^double a] a)
;java.lang.IllegalArgumentException: Mismatched primitive return, expected: long, had: double
;当*warn-on-reflection*为true的时候,在试图使用封装的值去recur的时候,编译器会报警告
(set! *warn-on-reflection* true)
(loop [x 5]
  (when-not (zero? x)
    (recur (dec x))))
;编译器基于5推理出rec的类型是long,因而没有不一致的地方
;但如果使用dec',recur的参数就有可能是BigInt,这种情况就会触发编译器的反射警告
(loop [x 5]
  (when-not (zero? x)
    (recur (dec' x))))
;recur arg for primitive local: x is not matching primitive, had: Object, needed: long
;Auto-boxing loop arg: x
;如果传递一个不兼容的原始类型给recur,也会触发发射的警告
(loop [x 5]
  (when-not (zero? x)
    (recur 0.0)))
;recur arg for primitive local: x is not matching primitive, had: double, needed: long
;Auto-boxing loop arg: x
;编译器不止在函数体内检查类型,如果一个函数返回double,你把它的返回值传给recur,同样会触发警告
(defn dfoo ^double [^double a] a)
(loop [x 5]
  (when-not (zero? x)
    (recur (dfoo (dec x)))))
;recur arg for primitive local: x is not matching primitive, had: double, needed: long
;Auto-boxing loop arg: x
;这种情况可以使用原始类型强制函数long来避免触发警告
(loop [x 5]
  (when-not (zero? x)
    (recur (long (dfoo (dec x))))))
;这些原始类型强制函数大多数用于消除与反射或自动封装相关的警告,再看一个例子
(defn round
  [v]
  (Math/round v))
;Reflection warning,call to static method round on java.lang.Math can't be resolved (argument types: unknown).
(defn round
  [v]
  (Math/round (double v)))
;当用于其他场景时,类型强制函数返回它所指示的类型对应的值,和Java中的类型强制声明是一样的

;在必要的时候,使用原始类型数组是合理的,只要这个数组是函数的局部变量,这个函数还是符合幂等性的
;以实现一个frequencies来举例
(defn vector-histogram
  [data]
  (reduce (fn [hist v]
            (update-in hist [v] inc))
          (vec (repeat 10 0))
          data))
;看一下这个实现的效率
(def data (doall (repeatedly 1e6 #(rand-int 10))))
(time (vector-histogram data))
;Elapsed time: 98.706918 msecs
;使用long数组实现
(defn array-histogram
  [data]
  (vec
   (reduce (fn [^longs hist v]
             (aset hist v (inc (aget hist v)))
             hist)
           (long-array 10)
           data)))
(time (array-histogram data))
;Elapsed time: 23.919621 msecs

;如果想要把已有的集合转换成数组,可以使用into-array和to-array

;into-array返回的是集合的第一个元素的类型的数组,或是指定的超类型的数组
(into-array ["a" "b" "c"])
;#object["[Ljava.lang.String;" 0x6807f1a0 "[Ljava.lang.String;@6807f1a0"]

(into-array CharSequence ["a" "b" "c"])
;#object["[Ljava.lang.CharSequence;" 0x361b70f3 "[Ljava.lang.CharSequence;@361b70f3"]
;上面这种指定数组元素类型的方式在与Java进行互操作的时候比较有用

;这种方式也可以用于生成原始类型的数组
(into-array Long/TYPE [1 2 3])
;#object["[J" 0x13a24756 "[J@13a24756"]

;clojure提供了一些辅助函数用于创建原始类型的数组或引用数组:boolean-array,byte-array,short-array,char-array,int-array
;long-array,float-array,double-array,object-array
;它们的参数可以使一个指定数组大小的数字或一个集合
(long-array 10)
;#object["[J" 0x5880530d "[J@5880530d"]
(long-array (range 10))
;#object["[J" 0x2a6757a8 "[J@2a6757a8"]
;或者可以同时传入这两个参数,但如果集合的大小比数组的容量小,那么会使用这个数组的元素类型的默认值填充
(seq (long-array 20 (range 10)))
;(0 1 2 3 4 5 6 7 8 9 0 0 0 0 0 0 0 0 0 0)

;make-array用于创建任意大小或维度的数组,用所给类型的默认值初始化
(def arr (make-array String 5 5))
(aget arr 0 0)
;nil
(def arr (make-array Boolean/TYPE 10))
(aget arr 0)
;false
;当需要获取数组的类型,尤其是多维数组的类型的时候,需要使用class函数,但这样做必须有一个数组的实例,这样会让人感觉很奇怪
;还有一种做法是clj_operate_java.clj中的array-type函数
;clojure在不知道数组的类型的时候,访问和修改其中的元素会使用反射,这与把数组作为局部优化是相悖的
;所以clojure提供了一组专门用于数组的类型提示:
;^objects,^booleans,^bytes,^chars,^longs,^ints,^shorts,^doubles,^floats

;aget和aset也要求数组类型是已知的来避免使用反射
(let [arr (long-array 10)]
  (aset arr 0 50)
  (aget arr 0))

;map和reduce也是可以用于数组的,但会引起对原始类型的自动封装
;使用loop替代它们,但这样要自己跟踪管理数组的下标,容易出错
;为此,clojure提供了两个宏amap和areduce用来操作数组同时避免自动封装

(let [a (int-array (range 10))]
  (vec
   (amap a i res
         (inc (aget a i)))))
;[1 2 3 4 5 6 7 8 9 10]
;amap的第一个参数时源数组,第二个参数是下标的名称,第三个是结果数组的名称,第四个是一个表达式,它的结果会设置为结果数组的下标i的值

(let [a (int-array (range 10))]
  (areduce a i sum 0
           (+ sum (aget a i))))
;前面两个参数和amap是一样的,第三个参数是累加器的名称,第四个是累加器的初始值,最后是一个表达式,它的值会成为下一次迭代的累加器的值和迭代结束之后的返回结果

;当操作多维数组是,要特别小心
(def arr (make-array Double/TYPE 1000 1000))
(time (dotimes [i 1000]
        (dotimes [j 1000]
          (aset arr i j 1.0)
          (aget arr i j))))
;Elapsed time: 11969.139812 msecs
;可以看出上面的代码效率很低,原因是在操作多维数组的时候,aset被apply传播到其他参数时,1.0会被封装(因为apply的第一个参数是IFn类型)
;提高性能的方式是手动展开多维数组
(time (dotimes [i 1000]
        (dotimes [j 1000]
          (let [^doubles darr (aget ^objects arr i)]
            (aset darr j 1.0)
            (aget darr j)))))
;Elapsed time: 16.564624 msecs

;作者提供了两个宏来实现对多维数字自动解包和加类型提示
(defmacro deep-aget
  ([array idx]
   `(aget ~array ~idx))
  ([array idx & idxs]
   (let [a-sym (gensym "a")]
     `(let [~a-sym (aget ~(vary-meta array assoc :tag 'objects) ~idx)]
        (deep-aget ~(with-meta a-sym {:tag (-> array meta :tag)}) ~@idxs)))))

(defmacro deep-aset
  [array & idxsv]
  (let [hints '{booleans boolean,bytes byte
                chars char, longs long
                ints int ,shorts short
                doubles double,floats float}
        hint (-> array meta :tag)
        [v idx & sxdi] (reverse idxsv)
        idxs (reverse sxdi)
        v (if-let [h (hints hint)] (list h v) v)
        nested-array (if (seq idxs)
                       `(deep-aget ~(vary-meta array assoc :tag 'objects) ~@idxs)
                       array)
        a-sym (gensym "a")]
    `(let [~a-sym ~nested-array]
       (aset ~(with-meta a-sym {:tag hint}) ~idx ~v))))
;使用deep-aget和deep-aset实现之前的代码
(time (dotimes [i 1000]
        (dotimes [j 1000]
          (deep-aset ^doubles arr i j 1.0)
          (deep-aget ^doubles arr i j))))

;Elapsed time: 24.035916 msecs
;to-array总是返回Object数组
(to-array [1 2 3])
(into-array [1 2 3])

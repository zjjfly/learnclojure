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

(ns learnclojure.chapt1.core6)
;;函数字面量
;;下面这些匿名函数是一样的,第二个就是函数字面量
(fn [x y] (Math/pow x y))
#(Math/pow %1 %2)
;;后一个是前一个的reader语法糖，下面可证明
(read-string "#(Math/pow %1 %2)")
;;函数字面量没有隐式的使用do，fn有，所以字面量要显示的使用do
(fn [x y]
  (println (str x \^ y))
  (Math/pow x y))
#(do (println (str %1 \^ %2))
     (Math/pow %1 %2))
;;%用来指定函数参数个数以及引用具体参数
;;它还有两个小技巧
;;1.很多函数只接受一个参数，所以可以简单的用%引用它的第一个参数,下面两个字面量等价的
#(Math/pow % %2)
#(Math/pow %1 %2)
;;2.可以定义不定参数的函数，通过%&引用这些剩余参数,所以下面两个函数也是等价的
(fn [x & rest]
  (- x (apply + rest)))
#(- % (apply + %&))
;; 函数字面量不能嵌套
;;#(#(+ % %)) 运行出错

;;条件判断：if
;;if是clojure唯一的基本条件判断
;; if后第一个条件表达式是逻辑True（任何非nil和false的值）的话，那么整个表达式的值是第二个表达式的值，否则是第三个表达式的值
(if "hi" \t)
(if 43 \t)
(if nil "adad" \f)
(if false "adad" \f)
;;如果条件表达式是false，但else表达式没有提供，则整个if的值是nil，这种情况最好使用when
(if (not true) \t)
;;true?和false？这两个谓词和if条件判断无关,它们检查的是参数是否是布尔值true和false，而不是逻辑上的true和false
(true? "ad")
(if "ad" \t \f)
;;逻辑上的true和下面的式子等价
#(or (not  (nil? %)) (true? %))

;;循环：loop和recur
;;clojure有好几个有用的循环结构，比如doseq和dotimes，都是构建在recur之上的
;;recur能在不消耗堆栈空间的情况下吧程序执行转到上下文最近的loop头那里
;;loop形式最后一个表达式如果产生一个值，则这个值就作为loop形式的值
(loop [x 5]
  (println x)
  (if (neg? x)
    x
    (recur (- x 4))))
;;函数也可以建立loop头
(defn countdown
  "hehe"
  [x]
  (if (zero? x)
    :blastoff!
    (do (println x)
        (recur (dec x)))))
(countdown 5)
;;recur 是非常底层的循环和递归控制操作，通常不用，但它不消耗堆栈空间，对于实现某些递归算法很关键
;;并且它不对数字装箱，所以对于实现一些对于数学和数据有关的操作很有用
;;还有一些情况需要累计或者消费一个或多个集合，这也会用到recur



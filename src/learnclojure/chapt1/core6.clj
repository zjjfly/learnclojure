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


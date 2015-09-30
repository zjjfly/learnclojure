(ns learnclojure.chapt1.core7
  (:import (java.util ArrayList)))
;; 引用var：var
(def x 5)
x
;;有时候，想要获得指向var本身的引用，而不是var的值
(var x)
;;在repl中看到过很多var在其中的显示：#'后面跟一个符号
;;clojure中也有一个reader语法糖求值成var这个特殊形式
#'x

;;和java互操作:.和new
;;所有和java的互操作，初始化，静态和实例方法调用还有字段访问都是通过new和.这两个特殊形式实现的
;;还有一些语法糖使互操作更简洁，并且和clojure的风格一致。所有很少看到直接用.和new的。

;;对象初始化，java代码：new java.util.ArrayList(100)
;;语法糖
(ArrayList. 100)
;;特殊形式
(new ArrayList 100)

;;调用静态方法,java代码：Math.pow(2,10)
;;语法糖
(Math/pow 2 10)
;;特殊形式
(. Math pow 2 10)

;;调用实例方法，java代码："hello".substring(1,3)
;;语法糖
(.substring "hello" 1 3)
;;特殊形式
(. "hello" substring 1 3)

;;访问静态成员变量。java代码：Integer.MAX_VALUE
;;语法糖
Integer/MAX_VALUE
;;特殊形式
(. Integer MAX_VALUE)

;;访问实例成员变量
;;java代码：someObj.someField
;;语法糖：(.someField someObj)
;;特殊形式：(. someObj someField)

;;异常处理,使用try和throw两个特殊形式

;;状态修改，set！

;;锁原语:monitor-enter和monitor-exit，但一般用宏locking


;;eval函数接受一个clojure形式，然后求出这个形式的值。但大多数使用eval的地方可以用宏来解决
(eval :foo)
(eval [1 2 3])
(eval "ad")
(eval '(learnclojure.charpt1.core1/average [60 80 100 400]))

;;现在可以自己实现一个clojure的repl了
;;用read-string读取字符串，用eval执行表达式
(defn embedded-repl
  "A native Clojure REPL implementation.Enter ':quit' to exit"
  []
  (print (str (ns-name *ns*) ">>>"))
  (flush)
  (let [expr (read)
        value (eval expr)]
    (when (not= :quit value)
      (println value)
      (recur))))
(embedded-repl)
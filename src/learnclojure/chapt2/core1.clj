(ns learnclojure.chapt2.core1
  (:import (learnclojure.chapt2 StatefulInteger)))
;;函数式编程的特点：
;;1.喜欢操作不可变值
;;2.喜欢对数据进行声明式的处理
;;3.喜欢通过函数组合、高阶函数和不可变数据结构处理复杂问题
;;值不会随时间的改变改变
(= 5 5)
(= 5 (+ 2 3))
(= "boot" (str "bo" "ot"))
(= nil nil)
(let [a 5]
  (= a 5))
;; 利用java实现的可变Integer，看看integer可变了会怎么样
(def five (StatefulInteger. 5))
(def six (StatefulInteger. 6))
(.intValue five)
(.intValue six)
(= five six)
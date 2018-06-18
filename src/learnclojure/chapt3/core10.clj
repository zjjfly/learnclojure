(ns learnclojure.chapt3.core10)

;;元数据
;;用途:1.访问控制和类型声明;2.指定注解

;;clojure的元数据是一个以^修饰的map
(def a ^{:create (System/currentTimeMillis)}
  [1 2 3])
;;获取元数据
(meta a)
;;如果某个关键字对应的值是布尔类型的true,那么可以用下面这种简单的方式书写:
(meta ^:private [1 2 3])
;={:private true}
(meta ^:private ^:dynamic [1 2 3])
;={:dynamic true, :private true}

;;可以使用with-meta和vary-meta来克隆对象并修改它的元数据
(def b (with-meta a (assoc (meta a)
                           :modified (System/currentTimeMillis))))
(meta b)
;={:create 1529205491839, :modified 1529205875947}
;;vary-meta和with-meta的区别是:with-meta把一个值的元数据完全替换成新的元数据,而vary-meta是通过给定的更新函数以及参数对当前的元数据进行更新
(def bb (vary-meta a assoc :create (System/currentTimeMillis)))
(meta bb)

;;元数据不会影响打印结果,不会影响相等性
(= a b)
;=true
(= ^{:a 5} 'any-value
   ^{:b 5} 'any-value)
;=true

;;一个有元数据的不可变集合进行修改操作后返回的集合一样有原来集合的元数据
(meta (conj a 100))
;={:create 1529205491839}
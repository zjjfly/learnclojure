(ns learnclojure.chapt3.core4)
;;Associative抽象
;;关系型数据结构Associative接口抽象的是把一个key和一个value关联起来的数据结构
;;这个接口有四个操作：
;;1.assoc 它向集合中添加一个新的key到value的映射
;;2.dissoc 从集合中移除指定的key到value的映射
;;3.get 从集合中取出指定key的value
;;4.contains？ 是一个谓词，集合中包含指定key的时候返回true，否则返回false

;;最正宗的关系型数据结构是map，它也是clojure提供的最有用的数据结构
(def m {:a 1,:b,2,:c 3})
(get m :a)
(get m :d)
(get m :d "not found")
(assoc m :d 4)
(dissoc m :b)
;;assoc和dissoc还可以增加和删除多个键值对
(assoc m
  :x 4
  :y 5
  :z 6)
(dissoc m :a :c)

;;虽然这些函数通常用来操作map，但get和assoc也可以用来操作vector
;;vector也可以看出关系型数据结构，只是它的key是数组下标
(def v [2 6 9])
(get v 1)
(get v 10)
(get v 10 "not found!")
;;assoc可以更新数组中某个下标对应的值
(assoc v
  1 4
  0 -12
  2 :p)



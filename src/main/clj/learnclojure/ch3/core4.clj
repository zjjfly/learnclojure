(ns learnclojure.ch3.core4
  (:import (java.util HashMap)))
;;Associative抽象
;;关系型数据结构Associative接口抽象的是把一个key和一个value关联起来的数据结构
;;这个接口有四个操作：
;;1.assoc 它向集合中添加一个新的key到value的映射
;;2.dissoc 从集合中移除指定的key到value的映射
;;3.get 从集合中取出指定key的value
;;4.contains？ 是一个谓词，集合中包含指定key的时候返回true，否则返回false

;;最正宗的关系型数据结构是map，它也是clojure提供的最有用的数据结构
(def m {:a 1, :b, 2, :c 3})
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
;;可以用assoc实现和conj一样的效果，把一个值添加到vector最后，但需要显式的告诉vector新的下标
(assoc v 3 "ddd")
;;get还能操作set，如果一个key在set中存在，会返回它
(def s #{1 2 4})
(get s 1)
(get s 3)
(get s 3 "not found!")
;;它的返回值意味着set里面是值到值本身的映射，这使得set可以满足get的语义，同时也和条件判断里的经典用法一致
(if (get s 3)
  (print "it contains '3'")
  (print "it doesn't contain '3'"))
;;clojure中map和set本身就支持get语义
(s 1)
(m :b)

;;contains?是一个谓词，作用是检查集合中是否包含指定的key
(contains? m :d)
(contains? v 3)
(contains? s 3)
;;初学者会用contains？检测vector中是否含有指定的值，这是不对的.contains？检测是集合中是否包含某个key
;;检测集合中是否包含某个值用some

;;get和contains？非常通用，可以高效的操作vector,map,set,java里的map，字符串以及java数组
(get "clojure" 3)
(contains? (HashMap.) 1)
(get (into-array [1 2 3]) 0)

;;小心nil
;;如果集合中不包含某个key，调用的时候也没有设置默认值,那么会返回nil、但这个key对于的值也可能是nil
(get {:ethl nil} :ethl)
;;那么怎么知道是没有这个key还是这个key的值是nil呢？
;;我们可以使用find,如果map里包含指定的key的话,它返回的是键值对;如果不包含这个key,则返回nil
(find {:ethl nil} :ethl)
(find {:ethl nil} :r)
;;find也很容易和解构形式if-let，when-let一起用
(if-let [e (find {:a 5 :b 6} :a)]
  (format "found %s=>%s" (key e) (val e))
  "not found")
(if-let [[k v] (find {:a 5 :b 6} :a)]
  (format "found %s=>%s" k v)
  "not found")
;;当被用在条件判断中的时候，false在关系型集合中和nil有同样的问题，解决方法和nil一样



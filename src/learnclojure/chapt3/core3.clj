(ns learnclojure.chapt3.core3)
;;惰性序列
;;一个集合的内容可以惰性生成，这种情况下集合的元素是一个函数调用的结果
;; 只有真正需要集合里的元素的时候采才去调用函数计算一次，而且只算一次，访问一个惰性序列的过程叫实例化
(lazy-seq [1 2 3])
(defn random-ints
  [limit]
  (lazy-seq
    (println "realizing random int")
    (cons (rand-int limit)
          (random-ints limit))))
//定义一个惰性序列，但不会实例化
(def rands (take 10 (random-ints 10)))
//求第一个元素，使我们定义lazy-seq时传入的表达式被调用一次，得到一个随机数，并打印一次信息
(first rands)
//求中间一个元素，在这个元素之前的还没有实例化的元素都会被实例化
(nth rands 3)
//对所有的元素实例化（已经实例化过的会被保存，不用再实例化）
(count rands)
;;cons，list*不会强制对传入的序列（可能是惰性的）求值
;;这一点使得我们可以传入一个或多个值以及一个惰性序列来创建一个更大的惰性序列
;;random-ints实现的很差，一般不这么写，更好的写法是：
(repeatedly 10 (partial rand-int 10))


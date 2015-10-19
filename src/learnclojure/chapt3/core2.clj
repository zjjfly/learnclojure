(ns learnclojure.chapt3.core2)
;;Collection
;;clojure所以的数据结构都实现了Collection接口
;;核心函数：
;;用conj来添加一个元素到集合
;;用seq来获取集合的顺序视图
;;用count来获取集合元素的个数
;;用empty来获取一个和提供集合类型一样的空集合
;;用=来判断两个或多个集合是否相等

;;empty不需知道一个集合的具体类型就能创建一个同类型的集合
;;例子：一个可以交换顺序集合中两个元素位置的函数
;;interleave返回包含每个集合的第一个元素，第二个元素，第三个元素......的lazy seq
(defn swap-pairs
  [sequential]
  (into (empty sequential)
        (interleave
          (take-nth 2 (drop 1 sequential))
          (take-nth 2 sequential))))
;;take-nth返回一个包含传入的集合的第0个元素，第n个元素，第2n个元素....的lazy seq
(take-nth 2 (range 10))
;;=(0 2 4 6 8)
;;由于into的多态性和empty，使swap-pairs返回的类型和参数类型一致
(swap-pairs (range 10))
(swap-pairs (apply vector (range 10)))

;;empty对map也一样，传入有序的就返回有序的，传入无序的就传回无序的
;;for是一个列表推导形式，产生一个lazy seq
(defn map-map
  [f m]
  (into (empty m)
        (for [[k v] m]
        [k (f v)])))
(map-map inc (hash-map :a 3 :b 5))
;={:b 6, :a 4}
(map-map inc (sorted-map :a 3 :b 5))
;={:a 4, :b 6}

;;count返回元素个数
(count '(1 2 4 5))
(count #{1 2 4 5})
(count {:a 1 :b 2 :C 4 :d 5})
(count [1 2 4 5])
;;count保证所有对于所有集合操作耗时都是高效的，序列除外（因为它的长度可能是未知的）


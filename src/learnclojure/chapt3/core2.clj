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

;;Sequence接口定义了一个获取和遍历各种集合的一个顺序视图的方法
;;常用函数：
;;seq返回传入参数的一个序列
;;first，rest，next提供了遍历序列的方法
;;lazy-seq创建一个内容是一个表达式结果的惰性序列

;;seq支持的参数类型：
;;1.所以clojure集合类型
;;2.所有java集合类型,即java.util.*
;;3.所有java map
;;4.所有java.lang.CharSequence，包括String
;;5.实现了java.lang.Iterable的类型
;;6.数组
;;7.nil（或者java方法返回的null）
;;8.任何实现了clojure.lang.Seqable接口的类型
;;一部分例子：
(seq "Clojure")
;=(\C \l \o \j \u \r \e)
(seq {:a 5 :b 6})
;=([:a 5] [:b 6])
(seq (java.util.ArrayList. (range 5)))
(seq (into-array ["Clojure" "Programming"]))
;=("Clojure" "Programming")
(seq [])
;=nil
(seq nil)
;=nil
;;seq对于nil和空集合都返回nil

;;许多和序列打交道的函数都隐式调用了seq,比如:
(map str "Clojure")
(set "Programming")

;;如果用现有的序列函数来定义自己的操作序列的函数，不需要对参数调用seq，除了使用lazy-seq

;;clojure标准库clojure.core也有很多方法创建操作序列，最基本的是first，rest，next
(first "Clojure")
(rest "Clojure")
(next "Clojure")
;;rest和next大多数情况是一样的，对待空集合和只有一个元素的集合它们不一样
(rest "")
;=()
(next "")
;=nil
(rest "1")
;=()
(next "1")
;=nil
(= (next "1")
   (seq (rest "1")))
;;这个区别看起来很小 ，但使我们很容易实现惰性序列

;;序列不是迭代器，
(doseq [x (range 3)]
  (println x))
;;下面的rst和r是两个不同的seq，它们不会影响彼此
(let [r (range 3)
      rst (rest r)]
  (prn (map str r))
  (prn (map #(+ 100 %) r))
  (prn (conj r -1) (conj rst 42)))

;;序列不是列表
;;序列和列表看上去很像，同时列表本身是自身的序列，但它们很多方面还是不同的：
;;1.要记算一个序列的长度比较耗时
;;2.序列的内容可能是惰性的，要真的用到序列中的值时才会去实例化
;;3.一个生产序列的函数可能生成无限惰性序列，所以也就不可数了
;;而列表保存了长度，所以获取它的长度很高效，而获取序列的长度的唯一方法就是遍历它
(let [s1 (range 1e6)]
  (time (count s1)))
(let [s2 (apply list (range 1e6))]
  (time (count s2)))
;;计算包含的元素个数是惰性序列实例化的方法之一

;;直接创建序列的两个方法：cons和list*
;;cons接受两个参数，第一个参数是一个值作为新seq的开头，第二个参数是一个集合作为新seq的尾巴
(cons 0 (range 1 5))
;;cons始终把第一个参数加到第二个参数集合的头，而你管集合的类型，所以它和conj不一样
(cons :a [:b :c])
;;list*是一个帮助函数，来简化指定多个值的创建seq的需求
(cons 0 (cons 1 (cons 2 (cons 3 (range 4 10)))))
;;等价于
(list* 0 1 2 3 (range 4 10))
;;这两个函数在写宏的时候用的多


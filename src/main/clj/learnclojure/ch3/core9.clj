(ns learnclojure.ch3.core9)
;;不可变性和持久性
;;clojure集合的两个重要特征是不可变性和持久性
(+ 1 2)
;=3
(def v (vec (range 1e6)))
(count v)
(def v2 (conj v 1e6))
(count v2)
;=1000001
(count v)
;=1000000
;;这里的v2是完全独立的数据结构，看起来conj(以及任何对clojure数据结构进行操作的函数)创建了它所操作的集合的完全拷贝
;;不过，实际情况不是这样的
;;对于clojure不可变数据结构的操作是非常高效的，通常跟java里对应的操作是一样高效的。这是因为clojure的数据结构是持久的。
;;这种技术使得你做过修改的集合和原来的结合共享内部数据存储，而同时保证这个源集合产生出来的集合一样的高效率
;;为了实现这个持久性又不牺牲性能，clojure数据结构实现了一种结构共享技术。
;;意思是说，对于任何一个操作，clojure都不会去做一个深度拷贝，只有受影响的会被添加删除，而不变的那些元素还是共享老集合里面的那些。
;;这对于map和set也是一样的,在clojure中,它们都是使用tree来实现的
(def a {:a 5 :b 6 :c 7 :d 8})
(def b (assoc a :c 0))
(def c (dissoc a :d))
;;上面的a b c的很大一部分数据都是共享的,共享树的节点

;;不可变性的好处是:1.简化多线程编程 2.轻松版本化

;;可变集合
;;使用transient这个函数把不可变集合转成可变集合
(def x (transient []))
(def y (conj! x 1))
(assert (= (count x) (count y)))

;;可变集合适用于在需要对一个集合进行很多次conj操作的时候,这样可以提升性能
;;我们先自己实现一个into函数
(defn native-into [coll source]
  (reduce conj coll source))
(assert (= (native-into #{} (range 100))
           (into #{} (range 100))))

;;比较它和into的性能
(time (do
        (native-into #{} (range 1e6))
        nil))
(time (do
        (into #{} (range 1e6))
        nil))

;;基本上into比我们自己实现的快一倍，因为它使用了可变集合,我们也使用可变集合来实现一下
(defn fast-into
  [coll soure]
  (persistent! (conj! (transient coll) soure)))
(time (do
        (fast-into #{} (range 1e6))
        nil))

;;这种使用可变集合的方式是可以接受的,因为可变集合没有逸出s还有它的函数的范围
;;而且宏观的看这个函数式接收不可变集合返回不可变集合,和native-into的语义是一样的

;;要记住,只有vector和无序的map和无序的set有可变集合版本
;;目前没有一个函数可以检测一个集合对象是否有可变的版本,我们可以自己实现一个:
(defn transient-capable?
  [coll]
  (instance? clojure.lang.IEditableCollection coll))

;;transient这个函数对原集合不会有影响
(def v [1 2])
(def tv (transient v))
(conj v 3)
;[1 2 3]
;;而相反的是persistent!会让原来的可变集合不可用
(persistent! tv)
;;下面的代码会抛出异常
;(tv 0)

;;可变集合也可以使用一些它的不可变版本的访问函数,还有seq也不支持
(nth (transient [1 2]) 0)
(get (transient {:a 1 :b 2}) :a)
;可变集合也是函数
((transient [1 2]) 0)
((transient {:a 1 :b 2}) :a)
;;但不是所有的,比如find就不行
(find (transient {:a 1 :b 2}) :a)

;;但是对于更新函数,都不支持,而是但都有对应的版本,名字和不可变的版本基本一样,只是在后面多了一个!,如conj!,assoc!,dissoc!,disj!,pop!
;;不可变集合调用更新函数之后,原来的集合就不可用了,所以需要使用这些函数的返回值
(let [tm (transient {})]
  (doseq [x (range 100)]
    (assoc! tm x 0))
  (persistent! tm))
;;修改后的版本:
(let [tm (transient {})]
  (persistent!
   (reduce #(assoc! %1 %2 0)
           tm (range 100))))
;;可变集合不能组合,persistent!不会遍历嵌套的可变集合
(persistent! (transient [(transient {})])) ;类型是PersistentArrayMap$TransientArrayMap
;;可变集合也是不可比较的
(= (transient [1 2]) (transient [1 2]))
;=false


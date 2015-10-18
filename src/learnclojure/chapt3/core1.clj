(ns learnclojure.chapt3.core1)
;;集合
;;基本数据集合有map，vector，set和列表,clojure提供了便利的字面量
;;几个例子
;;列表
'(1 2 4)
;;vector
['a 'b :name 12.6]
;;map
{:name "zjj" :age "25"}
;;set
#{1 2 3}
;;另一种map
{Math/PI "~3.14"
 [:composite "key"] 42
 nil "nothing"}
;;包含map的set
#{{:first-name "zi" :last-name "junjie"}
  {:first-name "Chen" :last-name "jiali"}}
;;clojure数据结构有两个特色
;;1.是根据抽象来使用的，而不是具体的实现细节
;;2.数据结构是不可改变和持久的

;;抽象由于实现
;;100个函数操作一个数据解构比10个函数操作10种数据解构要好（出自SICP书序）
;;vector的一些操作
(def v [1 2 3])
(conj v 4)
(conj v 4 5)
(seq v)
;;map的一些操作
(def m {:a 4 :b 5})
(conj m [:c 6])
(seq m)
;;set操作
(def s #{1 2 3})
(conj s 10)
(sorted-set )
(conj s 3 4)
(seq s)
;;list的操作
(def lst '(1 2 3))
;;list的conj操作是把要插入的数据放在list前面的
(conj lst 0)
;;=(0 1 2 3)
(conj lst 0 -1)
;;=(-1 0 1 2 3)
(seq lst)

;;显然，seq和conj对于它们所操作的集合类型是多态的
;;clojure的精髓是小而易用的编程接口，在接口上再构建辅助函数
;;例如into函数,它是建立在conj和seq之上的,所以能用于任何支撑conj和seq的值
(into v [4 5])
(into m [[:c 7] [:d 8]])
(into s [3 4])
//map的seq是一个键值对序列，conj会保留键值对解构
(into  [1] {:a 1} )
;;=[1 [:a 1]]

;;构建小而广泛支持的抽象是clojure设计的核心原则
;;clojure集合中实现的几个主要抽象：
;;1.Collection 2.Sequence 3.Associative 4.Indexed 5.Stack 6.Set 7.Sorted
;;接下来会细讲基于这些抽象来使用clojure的数据结构


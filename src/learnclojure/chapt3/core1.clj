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


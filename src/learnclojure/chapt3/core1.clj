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

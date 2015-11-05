(ns learnclojure.chapt3.core5)
;;Indexed接口
;;到目前为止，我们一直回避讨论怎么直接获取vector的第n个值，或者直接修改vector的第n个值
;;原因是下标是新的指针
;;顺序集合的下标对于实现算法很少是必须的，在很多情况下，代码涉及到了下标说明代码过于复杂了
;;如果实在要用到下标，那么Indexed这个接口提供了操作下标的函数
;;它只有一个函数，nth，它和get类似，不同之处是对越界下标的处理
(nth [:a :b :c] 2)
(get [:a :b :c] 2)
;;nth在越界的时候会报IndexOutOfBoundsException
;;(nth [:a :b :c] 3)
;;get返回nil，不报错
(get [:a :b :c] 3)

;;nth和get的意义是很不一样的。首先，nth只能接受数字作为查询的key.它可以作用于很多有下标概念的值。
;;比如vector、列表。序列、java数组、java列表、字符串、正则表达式的匹配数组等
;;get则更通用，它可以操作任何关系型的值。
;;它们另一个区别是，get对错误更容忍，当你传给它一个不支持的数据类型，它也返回nil而不是抛出异常
(get 42 0)
;;用nth会报错
;;(nth 42 0)
;;clojure中的vector本身就支持nth语义，比调用nth更方便
(def v [1 2 3])
(v 0)

;;栈 stack
;;clojure没有独立的栈集合，它通过三个操作来支持这种语义
;;用conj把一个值加入栈
;;用pop获取栈顶的元素，并且移除这个值
;;用peek获取栈顶的元素，但不移除这个值
;;list和vector都可以作为栈，栈顶都是conj可以高效添加元素的一端

;;list作为栈
(conj '() 1)
(conj '(2 1) 3)
(peek '(3 2 1))
(pop '(3 2 1))
(pop '(1))

;;vector作为栈
(conj [] 1)
(conj [1 2] 3)
(peek [1 2 3])
(pop [1 2 3])
(pop [1])

;;对一个空栈调用pop会报错
;;(pop [])

;;set接口
;;从set中删除一个元素用disj
(disj #{1 2 4} 1 2)
;;clojure.set提供了一些有用的函数，如subset?、superset?、union、intersection、project等

;;sorted 有序集合
;;实现sorted抽象的集合北一特定的顺序保存，顺序可以通过一个谓词函数或者一个特定的comparator接口来定义。
;;这使得我们可以高效，正序(或反序)的获取集合或者获取集合的一部分。这个接口包括以下函数
;;1.rseq函数可以在常量时间内反序的返回一个集合的函数
;;2.subseq函数可以返回一个集合的某一个区间的元素的序列
;;3.rsubseq，和subseq类似，但返回的元素是反序的

;;只有map和set实现了sorted接口，没有字面量来表示sorted集合，要创建sorted集合可以用sorted-map和sorted-set来创建有序的map和set。
;;如果要用谓词或比较器来定义排序规则的话，要用sorted-map-by和sorted-set-by
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4))
sm
(rseq sm)
;;subseq的谓词函数必须是>、<、>=、<=
(subseq sm <= :c)
(subseq sm > :b <= :y)
(rsubseq sm > :b <= :y)
;;因为sm本身是有序的，对于相同的结果，在sm上调用这些函数比在普通seq上调用要快的多

;;compare函数定义默认排序 正序。它支持clojure所有的标量和顺序集合，它会按照字典排序法对每一层元素排序
(compare 2 2)
(compare "ab" "abc")
(compare ["a" "b" "c"] ["a" "b"])
(compare ["a" 2 0] ["a" 2])



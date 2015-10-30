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
(nth [:a :b :c] 3)
;;get返回nil，不报错
(get [:a :b :c] 3)

;;nth和get的意义是很不一样的。首先，nth只能接受数字作为查询的key.它可以作用于很多有下标概念的值。
;;比如vector、列表。序列、java数组、java列表、字符串、正则表达式的匹配数组等
;;get则更通用，它可以操作任何关系型的值。
;;它们另一个区别是，get对错误更容忍，当你传给它一个不支持的数据类型，它也返回nil而不是抛出异常
(get 42 0)
;;用nth会报错
(nth 42 0)
;;clojure中的vector本身就支持nth语义，比调用nth更方便
(def v [1 2 3])
(v 0)
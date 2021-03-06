(ns learnclojure.ch3.core5
  (:require [clojure.set :as cset]))
;Indexed接口
;到目前为止，我们一直回避讨论怎么直接获取vector的第n个值，或者直接修改vector的第n个值
;原因是下标是新的指针
;顺序集合的下标对于实现算法很少是必须的，在很多情况下，代码涉及到了下标说明代码过于复杂了
;如果实在要用到下标，那么Indexed这个接口提供了操作下标的函数
;它只有一个函数，nth，它和get类似，不同之处是对越界下标的处理
(nth [:a :b :c] 2)
(get [:a :b :c] 2)
;nth在越界的时候会报IndexOutOfBoundsException
;(nth [:a :b :c] 3)
;get返回nil，不报错
(get [:a :b :c] 3)

;nth和get的意义是很不一样的。首先，nth只能接受数字作为查询的key.它可以作用于很多有下标概念的值。
;比如vector、列表。序列、java数组、java列表、字符串、正则表达式的匹配数组等
;get则更通用，它可以操作任何关系型的值。
;它们另一个区别是，get对错误更容忍，当你传给它一个不支持的数据类型，它也返回nil而不是抛出异常
(get 42 0)
;用nth会报错
;(nth 42 0)
;clojure中的vector本身就支持nth语义，比调用nth更方便
(def v [1 2 3])
(v 0)

;栈 stack
;clojure没有独立的栈集合，它通过三个操作来支持这种语义
;用conj把一个值加入栈
;用pop获取栈顶的元素，并且移除这个值
;用peek获取栈顶的元素，但不移除这个值
;list和vector都可以作为栈，栈顶都是conj可以高效添加元素的一端

;list作为栈
(conj '() 1)
(conj '(2 1) 3)
(peek '(3 2 1))
(pop '(3 2 1))
(pop '(1))

;vector作为栈
(conj [] 1)
(conj [1 2] 3)
(peek [1 2 3])
(pop [1 2 3])
(pop [1])

;对一个空栈调用pop会报错
;(pop [])

;set接口
;从set中删除一个元素用disj
(disj #{1 2 4} 1 2)
;clojure.set提供了一些有用的函数，如subset?、superset?、union、intersection、project等
(cset/subset? #{1 2} #{1 2 3})
(cset/superset? #{1 2} #{1 2 3})
;合集
(cset/union #{1 2} #{1 2 3})
;交集
(cset/intersection #{1 2} #{1 2 3})
(cset/project #{{:a 2 :b 3 :c 4} {:a 1 :b 2}}  [:a :b])

;sorted 有序集合
;实现sorted抽象的集合被一特定的顺序保存，顺序可以通过一个谓词函数或者一个特定的comparator接口来定义。
;这使得我们可以高效，正序(或反序)的获取集合或者获取集合的一部分。这个接口包括以下函数
;1.rseq函数可以在常量时间内反序的返回一个集合的函数
;2.subseq函数可以返回一个集合的某一个区间的元素的序列
;3.rsubseq，和subseq类似，但返回的元素是反序的

;只有map和set实现了sorted接口，没有字面量来表示sorted集合，要创建sorted集合可以用sorted-map和sorted-set来创建有序的map和set。
;如果要用谓词或比较器来定义排序规则的话，要用sorted-map-by和sorted-set-by
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4))
(conj sm [:d 5])
(rseq sm)
;subseq的谓词函数必须是>、<、>=、<=
(subseq sm <= :c)
(subseq sm > :b <= :y)
(rsubseq sm > :b <= :y)
;因为sm本身是有序的，对于相同的结果，在sm上调用这些函数比在普通seq上调用要快的多

;compare函数定义默认排序 正序。它支持clojure所有的标量和顺序集合，它会按照字典排序法对每一层元素排序
(compare 2 2)
(compare "ab" "abc")
(compare ["a" "b" "c"] ["a" "b"])
(compare ["a" 2 0] ["a" 2])
;compare不仅支持字符串，数字以及顺序集合，它还支持任何实现java.lang.Comparable接口的值，包括布尔值、关键字、符号
;以及所有实现了这个接口的java类，compare是很强大的函数，是默认的比较器

;比较器是一个接受两个参数的函数，如果第一个参数大于第二个参数，就返回正数；如果第一个参数小于第二个参数则返回负数，如果相等则返回0
;clojure里面所有的函数都实现了java.util.Comparator接口，所有它们都是潜在的比较器，但明显并不是所有函数都是设计来作为比较器的
;也不需要一定实现Comparator接口才能实现一个比较器，任何一个两个参数的谓词都可以。
;不用实现一个特定接口就能定义一个比较器,也意味着可以很容易定义多层排序：先按照一个规则排序，在这个基础上再按另一个规则排序。
;要实现只要定义一个高阶函数就可以了。
;比较函数可以直接传给有序集合的工厂函数，如sorted和sorted-by(或者任何一个java.util.Comparator作为参数的JavaAPI)
;生成一个有10个元素的惰性序列，并从小到大排序
;里面的compare原来是<,我换成了compare，这样惰性序列的元素可以是任意实现了comparable接口的类的实例
(sort compare (repeatedly 10 #(rand-int 100)))
;map-indexed函数返回一个lazy-seq，它的参数是一个函数f和一个集合col，
;返回值的第一个元素是(f [0 col0]),第二个元素是(f [1 col1]).....
;下面的表达式是对一个元素是多个vector的lazy-seq排序结果，按照它的vector的第一个元素从大到小排序
(sort-by first > (map-indexed vector "clojure"))
;sort用于元素是非集合的集合排序，而sorted by 对于元素是集合的集合排序，它们的比较器参数可以省略，会默认使用compare

;clojure怎么把一个谓词变成一个比较器的呢？
;对于谓词fn，先顺序调用谓词(fn a b)如果为true，返回-1否则反序调用谓词(fn b a)，如果是true返回1，否则返回0
;comparator就是使用这个原理把一个两参谓词转换成一个比较器函数的
(def less (comparator <))
(less 1 4)
;=-1
(less 4 1)
;=1
(less 1 1)
;但我们很少这么干，因为clojure里面接受比较器作为参数的函数都默认做了这个转换，而且两参函数已经实现了java.util.Comparator接口

;所以，sorted-map和sorted-set是通过compare来定义默认规则进行排序的map和set
;sorted-map-by和sorted-set-by接受一个比较器(任何两参的微词函数也可以)来定义排序规则
;除了compare之外，能传给有序集合的最简单的比较器可能是(comp - compare)了，正好定义了compare的反序
(sorted-map-by compare :z 5 :x 9 :y 0 :b 2 :a 3 :c 4)
(sorted-map-by (comp - compare) :z 5 :x 9 :y 0 :b 2 :a 3 :c 4)

;需要注意，排序规则在有序map和有序set中也同时定义了两个元素是否相等，这个特点有时候会产生逻辑正确但让人吃惊的结果。
;例子:有一个函数来计算参数是10的几次方
(defn magnitude
  [x]
  (-> x Math/log10 Math/floor))
(magnitude 100)
(magnitude 100000)
;现在用magnitude创建一个谓词函数
(defn compare-magnitude
  [a b]
  (neg? (- (magnitude a) (magnitude b))))
(def comparator-magnitued (comparator compare-magnitude))
(comparator-magnitued 10 10000)
(comparator-magnitued 10000 10)
(comparator-magnitued 10 66)
;当我们把这个谓词函数用在一个有序集合里面时，有趣的事情发生了
(sorted-set-by compare-magnitude 10 1000 500)
;*1绑定的是最近在repl中打印的值
(conj *1 600)
;=#{10 500 1000}
(conj *1 750)
;=#{10 500 1000}
(contains? *1 1239)
;=true
;把600和750放入集合，集合没有变化，这是因为它们都是10的二次方因此对于比较器来说，它们和500是相等的，500已在集合中，所以不会把它们加进去
;类似的，1239和1000是等价的，所以用contains？来检查集合中是否包含1239的时候返回true；

;有时候比较器行为是你预期的，有时候则不是，比较器的语义我们可以完全控制，
;因此虽然用已有的谓词作为比较器比较方便，时也可以选择直接返回正数负数或0来使得比较器相等性更符合预期
;修改后的compare-magnitude:
(defn compare-magnitude
  [a b]
  (let [diff (- (magnitude a) (magnitude b))]
    (if (zero? diff)
      (compare a b)
      diff)))
(sorted-set-by compare-magnitude 10 1000 500)
;=#{10 500 1000}
(conj *1 600)
;=#{10 500 600 1000}
(disj *1 750)
;=#{10 500 600 1000}
;现在集合中的值按照10的量级排序了，同时那些只依赖数字相等性的操作(如conj和disj)的结果和我们预期的一样

;subseq和rsubseq也能继续按照比较器所定义的顺序正常截取集合的一段
(sorted-set-by compare-magnitude 10 1000 500 670 1239)
;=#{10 500 670 1000 1239}
(def ss *1)
(subseq ss > 669)
;=(670 1000 1239)
(subseq ss > 670 <= 1000)
;=(1000)
(rsubseq ss > 500 <= 1000)
;=(1000 670)
;我们提拔方法>、>=、<、<=来指定需要的元素区间，其他比较器也是可以的

;应用：线性牛顿插值
(defn interpolate
  "take a collection of points,return a function which is a linear interpolation between those points."
  [points]
  (let [results (into (sorted-map) (map vec points))]
    (fn [x]
      (let [[xa ya] (first (rsubseq results <= x))
            [xb yb] (first (subseq results > x))]
        (if (and xa xb)
          (/ (+ (* ya (- xb x)) (* yb (- x xa)))
             (- xb xa))
          (or ya yb))))))
;ya*xb-ya*x+yb*x-yb*xa
;(map vec points)确保每个点都是vector表示的，从而可以加入map
;测试一下，有已知的三个点[0 0] [10 10] [15 5]
(def f (interpolate [[0 0] [10 10] [15 5]]))
(map f [2 10 12])
;=(2 10 8)
;perfect!

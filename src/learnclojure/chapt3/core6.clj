(ns learnclojure.chapt3.core6)
;;访问集合元素的简洁方法

;;clojure的集合以及大部分作为集合里面元素的类型本身也是函数，所以不必以至于get和nth这样冗长的方式访问集合元素
;;所以
(get [:a :b :c] 2)
(get {:a 5 :b 6} :b)
(get {:a 5 :b 6} :c 7)
(get #{1 2 3} 3)
;;和下面的这些更简洁的表达式是等价的
([:a :b :c] 2)
({:a 5 :b 6} :b)
({:a 5 :b 6} :c 7)
(#{1 2 3} 3)
;;上面的例子中，集合被放在函数的位置上，调用的参数是对应的key或下标。map跟get类似，可以接受第二个参数，如果没查到返回的默认值
;;vector和set值接受一个参数的调用形式。传递给vector的数组下标必须在vector的下标范围内，和nth一样
;;([:a :b :C] -1)
;;java.lang.IndexOutOfBoundsException

;;集合的key（通常）也是函数。类似的，最常见的key类型，关键字和符号也是函数，语义就是到集合中把最忌或者自己对应的集合找出来
;;所以这些:
(get {:a 5 :b 6} :b)
(get {:a 5 :b 6} :c 7)
(get #{:a :b :c} :d)
;;和下面的这些更简洁的表达式是等价的
(:b {:a 5 :b 6} )
(:c {:a 5 :b 6} 7)
(:d #{:a :b :c})
;;因为这个值在函数位置上，所以数字下标是不能用的，因此vector的查找不能用这种方式

;;这两种方法分别在什么时候用比较好呢？通常推荐把关键字或符号作为查找函数来使用。好处是，可以避免NillPointException
;;因为关键字、符号都是字面量不可能是null,比如：
(defn get-foo
  [map]
  (:foo map))
(get-foo nil)
;=nil
(defn get-bar
  [map]
  (map :bar))
;;(get-bar nil)
;=java.lang.NullPointerException
;;而且，(coll :foo)这个形式假定coll这个集是一个函数，这个假定对于有些集合不成立的，如列表，而且实现了clojure集合接口的数据结构不一定是函数.
;;所以，使用(:foo coll)这个形式更好，因:foo是字面量，它始终是函数，而且绝对不可能为nil。
;;当然，如果集合的key不是关键字或符号类型，那么只能用集合本身或者get或者nth来作为查找函数了

;;集合、key以及高阶函数
;;因为关键字、符号以及很多集合都是函数，使用它们作为高阶函数的输入函数是很常见同时也是很方便的做法
(map :name [{:age 21 :name "David"}
            {:gender :f :name "Suzanne"}
            {:name "Sara" :location "NYC"}])
;;some函数在一个序列里搜索第一个能够符合指定谓词的元素，把它和set一起用非常常见
(some #{1 3 7} [0 2 4 5 6])
;=nil
(some #{1 3 7} [0 2 3 4 5 6])
;=3
;;这使得some非常适合用在条件判断中来判断集合里面是否包含某个元素
;;一种更通用的方法是使用filter，它返回一个惰性序列，内容是满足给定谓词的元素
;;同样，可以直接把集合或者关键字或者符号作为谓词使用
(filter :age [{:age 21 :name "David"}
            {:gender :f :name "Suzanne"}
            {:name "Sara" :location "NYC"}])
;=({:age 21, :name "David"})
(filter (comp (partial <= 25) :age)  [{:age 21 :name "David"}
             {:gender :f :name "Suzanne" :age 20}
             {:name "Sara" :location "NYC" :age 34}])
;=({:name "Sara", :location "NYC", :age 34})

;;remove作用和filter相反，它把符合谓词的元素从集合中去除掉，返回所有不符合谓词要求的元素。相当于(filter (complement f) collection)

;;收set来测试结合中是否包含某个元素非常方便，但不要忘记，如果检查的元素是nil或false，那么结果可能就和我们预期的不一样了。
;;因为这两个值在逻辑上就是false
(remove #{5 7} (cons false (range 10)))
;=(false 0 1 2 3 4 6 8 9)
(remove #{5 7 false} (cons false (range 10)))
;=(false 0 1 2 3 4 6 8 9)
;;注意到，第二个式子想删除false没有删掉，所以，当我们不确定集合是否会包含nil或false元素时，最好用contains？而不是get之类的直接调用
(remove (partial contains? #{5 7 false}) (cons false (range 10)))
;;=(0 1 2 3 4 6 8 9)


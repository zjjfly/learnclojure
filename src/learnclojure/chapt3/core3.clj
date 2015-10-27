(ns learnclojure.chapt3.core3)
;;惰性序列
;;一个集合的内容可以惰性生成，这种情况下集合的元素是一个函数调用的结果
;; 只有真正需要集合里的元素的时候采才去调用函数计算一次，而且只算一次，访问一个惰性序列的过程叫实例化
(lazy-seq [1 2 3])
(defn random-ints
  [limit]
  (lazy-seq
    (println "realizing random int")
    (cons (rand-int limit)
          (random-ints limit))))
;;定义一个惰性序列，但不会实例化
(def rands (take 10 (random-ints 10)))
;;求第一个元素，使我们定义lazy-seq时传入的表达式被调用一次，得到一个随机数，并打印一次信息
(first rands)
;;求中间一个元素，在这个元素之前的还没有实例化的元素都会被实例化
(nth rands 3)
;;对所有的元素实例化（已经实例化过的会被保存，不用再实例化）
(count rands)
;;cons，list*不会强制对传入的序列（可能是惰性的）求值
;;这一点使得我们可以传入一个或多个值以及一个惰性序列来创建一个更大的惰性序列
;;random-ints实现的很差，一般不这么写，更好的写法是：
(repeatedly 10 (partial rand-int 10))
;;只包含一个参数的repeatedly会返回一个有无限元素的惰性序列，clojure中处理无限长度的序列很平常
;;clojure标准库和社区的一些库有很多透明处理惰性序列的函数，而且所有这些核心的序列处理函数都返回惰性序列
;;比如：map、for、filter、take、drop以及由它们引申出来的一些函数（take-nth、remove、drop-while等）
;;有了这些工具我们可以把许多问题归结为对一个序列的值的处理

;;如果序列中每个元素的实例化需要一些io操作或者计算量很大，那么要非常小心的处理。这也是rest和next很大的一个区别所在。
(def x (next (random-ints 10)))
;;next实例化两次
(def x (rest (random-ints 10)))
;;rest实例化一次

;;顺序解构始终使用next而不是rest
(let [[_ & _] (random-ints 10)])
;;实例化两次

;;要完全实例化一个惰性序列，如果需要保持这个序列的所以元素，用doall，只想要实例化的时候的副作用，用dorun
(doall (take 5 (random-ints 10)))
(dorun (take 5 (random-ints 10)))

;;定义惰性序列的代码应该尽量不要有副作用
;;因为惰性序列在定义的时候没有实例化，我们很难跟踪在哪里在什么时候产生的副作用
;;有时候甚至不会有副作用，根本无法确定。
;;惰性序列的实例化有时候会批量处理，所以更不知道什么时候发生副作用了
;;所以不能靠惰性序列的实例化控制程序流程
;;clojure用惰性序列的目的是可以透明的处理无法放入内存的大数据
;;使得处理大数据、小数据可以用统一的声明，可以用管道的方式表达处理算法
;;这种情况下序列可以看做是计算的中间载体而不是集合

;;会经常在clojure中看到这样的用法：给定一个或多个数据源，从数据源抽出一个序列，处理这个
;;序列，返回一个更加合适的数据结构，例子：
(apply str (remove (set "aeiouy")
                   ;;下面这个字符串会隐式的转化成一个字符序列
                   "vowels are useless!or maybe not..."))

;;只要保持了对序列的一个引用，那么序列中的元素不能被垃圾回收，这就是“头保持”
;;如果序列很大，容易发生内存溢出问题
;;split-with是这样一个函数：给它一个谓词函数，一个可序列化的值，返回两个惰性序列，第一个满足这个谓词函数的，第二个是不满足的,例子：
(split-with neg? [-2 -1 0 1 2])
;;下面这段代码就有头保持问题
;(let [[t d] (split-with #(< % 4) (range 1e8))]
  ;[(count d) (count t)])
;;把部分代码交换一下就不会出问题
(let [[t d] (split-with #(< % 4) (range 1e8))]
[(count t) (count d)])
;;原因是由于编译器发现序列的前四个元素还会被使用，所有没有回收这些元素，导致整个惰性序列都实例化了但没有被回收
;;所有会占用非常多内存
;;参考资料：http://xumingming.sinaapp.com/977/clojure-lazyseq-head-retention/
;;https://groups.google.com/forum/?fromgroups=#!topic/clojure/bTAYeLXc25w

;;向map、set中插入元素，=函数或者count函数都是头保持问题的诱因，因为它们都会强制对惰性序列实例化


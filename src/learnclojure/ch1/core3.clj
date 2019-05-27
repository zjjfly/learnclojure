(ns learnclojure.ch1.core3)
;;do 会依次求传入的clojure形式，并把最后一个的结果返回
(do (println "hi") (apply * [1 3 6]))
;;fn,let,loop,try,defn以及这些形式的变种都隐式的用了do,所以在这些形式中可以用多个表达式
(let [a (inc (rand-int 6))
      b (inc (rand-int 6))]
  (println (format "you rolled a %s and b %s" a b))
  (+ a b))

;;let用法:1.本地绑定 2.解构
;;本地绑定,所有本地绑定是不可变的
(defn hypot
  [x y]
  (let [x2 (* x x)
        y2 (* y y)]
    (Math/sqrt (+ x2 y2))))
(hypot 3 4)
;;如果不关心let中的某个表达式的,而是只需要它的副作用,比如打印日志,那么可以使用_
(do (let [r (rand-int 10)
          _ (println "随机数:" r)]))
;;clojure的vector
(def v [42 "foo" 99.2 [5 12]])
;;访问vector的几种方法
(first v)
(second v)
(last v)
;;nth返回v的某个index的值
(nth v 2)
(v 2)
;;vector实现了java的list接口，所以可以使用get方法
(.get v 2)
(+ (first v) (v 2))
(+ (first v) (first (v 3)))
;;let可以对任何顺序集合进行解构，包括：
;;1.clojure原生的list.vector，seq
;;2.java的list接口的实现
;;3.java的数组
;;4.字符串
(let [[x _ z] v]
  (+ x z))
;;下面这个和上面等价
(let [x (v 0)
      y (v 1)
      z (v 2)]
  (+ x z))
(let [[_ y] "bn"]
  y)
;;_可以忽略某个绑定，只关心它的副作用，可以用来print一些信息
;;内嵌的vector的里的值也可以解构出来，但最好不要内嵌太多层
(let [[x _ _ [y z]] v]
  (+ x y z))
;;解构可以保存剩下的元素,这是clojure里函数剩余参数的基础
;;这里rest是一个序列
(let [[_ _ & rest] v]
  rest)
;;解构还可以保持被解构的值，用:as,类似Scala中的@
(let [[x _ z :as origin-vector] v]
  (conj origin-vector (+ x z)))

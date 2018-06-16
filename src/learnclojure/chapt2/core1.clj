(ns learnclojure.chapt2.core1
  (:import (learnclojure.chapt2 StatefulInteger))
  (:require [clojure.string :as string]))
;;函数式编程的特点：
;;1.喜欢操作不可变值
;;2.喜欢对数据进行声明式的处理
;;3.喜欢通过函数组合、高阶函数和不可变数据结构处理复杂问题
;;值不会随时间的改变改变
(= 5 5)
(= 5 (+ 2 3))
(= "boot" (str "bo" "ot"))
(= nil nil)
(let [a 5]
  (= a 5))
;; 利用java实现的可变Integer，看看integer可变了会怎么样
(def five (StatefulInteger. 5))
(def six (StatefulInteger. 6))
(.intValue five)
(.intValue six)
(= five six)
(.setInt five 6)
;;通过重新给five设置值，five和six相等了。这样会非常麻烦
(= five six)
;;可变对象和方法对象一起使用的后果
(defn print-number
  [n]
  (print (.intValue n))
  (.setInt n 42))
;; 调用print-number方法，不仅会打印six，还会改变six的值，这是不可接受的
(print-number six)
(= five six)
(= six (StatefulInteger. 42))
;;在不可变对象很少被使用的语言中,对于一个简单的问题,比如把集合作为map的key实现起来很困难
;;而clojure不用担心这种问题，因为clojure提供的数据结构都是不可变和高效的
(def h {[1 2] 4})
(h [1 2])
;;conj函数调用返回的是一个新的vector,所以不用担心原来的key被修改
(conj (first (keys h)) 3)
(h [1 2])
h
;;可变对象的坏处:1.无法被安全的传递给函数调用
;;2.无法安全的作为map的key、sets的元素等
;;3.无法被安全的缓存
;;4.不能在多线程情况下安全的使用

;;函数作为头等公民和高阶函数
;;要定义一个函数，可以调用任意函数两次，用clojure很容易实现
(defn call-twice [f x]
  (f x)
  (f x))
(call-twice println 1)
;;要求一组数组最大值或把一个列表的字符串全部转成小写，用java写会很繁琐，用clojure可以很简单
;;clojure提供了不少高阶函数（接受函数作为参数或返回函数的函数），如map，reudce，partial等
;;map是其中最常用的,它接收一个函数，一个或多个集合作为参数，返回一个序列
;;(map f [x y z])和[(f x) (f y) (f z)]等价
;; (map f [x y z] [a b c])和[(f x a) (f y b) (f z c)]等价
(map string/lower-case ["Java" "Scala" "Clojure"])
(map * [1 3 5] [2 4 6])
;;有时候需要把一个集合合并成一个值，如求一个数组的最大值，要用到clojure的reduce函数。
;;我们把向一个集合应用一个函数产生单个值的过程叫归约，reduce就是用来归约的
;;求最大值
(reduce max [6 1 7 10])
;;便于理解，可以把上面这个式子写成
(max (max (max 6 1) 7) 10)
(max 6 1 7 10)
;;reduce可以有一个默认值,和上面的没什么不同，只是第一次调用函数时
;;提供的参数是默认值和集合的第一个元素
(reduce + 20 [1 2 3])
;;默认值使得我们可以把一个集合的元素转换成任意类型的值
(reduce
 (fn [m v]
   (assoc m v (* v v)))
 {}
 [1 2 3 6])

;;函数应用：apply
;;函数调用apply函数，apply参数就是函数的参数
;;这对于支持调用那种要调用的函数在运行时才确定，并且传给这个函数的参数个数是不一定的情况很有用
(apply hash-map [:a 5 :b 6])
;;apply在传给apply一个列表的不定参数之前可以先传给apply几个确定参数
(def args [2 -2 10])
(apply * 0.5 3 args)

;;偏函数：partial
;;把函数的部分参数传给一个函数，创建一个新的函数，这个新函数需要的参数则是你没有传给那个函数的剩余参数
(def only-strings (partial filter string?))
(only-strings ["a" 4 "b" 5])
;;偏函数很多情况下都很有用，比如一些函数需要一些配置才能进行工作(比如数据库地址，文件路径)
;;这种时候就可以用partial创建一个偏函数把这些配置信息包进去
;;这个特点使我们可以创建一些指定我们关心的那些参数的偏函数，而不用去理会我们不关心的那些参数

;;和普通的明确指定函数参数的调用相比，高阶函数apply和partial会有一些性能损耗，损耗很小，只在参数较多的时候会有



(ns learnclojure.ch4.core3
  (:require [learnclojure.ch4.common :as common]))

;;clojure的引用类型,一共四种:var,ref,agent,atom
;;它们是包含值的容器,容器里的值可以通过一些函数进行修改
;;所有的引用类型总是包含某个值(即使是nil),访问这个值都使用deref或@
@(atom 12)
@(agent {:c 1})
(map deref [(var +) (ref "http://clojure.org") (atom 12) (agent {:c 1})])

;;解引用返回的是一个快照,反映的是某一时刻的状态,这个快照是不可变值.引用类型中的值可以在获得这个快照后发生变化
;;对引用类型解引用是永远不会阻塞的,这和对future,delay,promise解引用不一样

;;引用类型除了可以解引用,还可以做到:
;;1.加一些元数据,需要使用alter-meta!
;;2.指定一些函数,在引用类型发生变化是会调用这些函数来通知我们.这些函数被称为观察器
;;3.指定一些函数,用于校验引用类型的状态是否符合某些条件,这些函数被称为校验器

;并发操作大致可以分为四类:
;1.协调,指多个线需要相互合作(至少不相互干扰)
;2.无需协调,指多个线程之间不互相影响,它们操作的数据是完全独立的
;3.同步,指调用线程会等待,阻塞,睡眠,直到获取指定上下文的独占访问
;4.异步,指线程不会阻塞在调用上,它可以继续做别的事情
;;这些分类的组合对应了不同的引用类型的使用场景:
;;=========================
;;      || 协调  || 非协调 ||
;;=========================
;;  同步 || ref  || atom  ||
;;=========================
;;  异步 ||      || agent ||
;;=========================
;;var没在这张图中,因为var主要用于线程内状态的修改

;;atom,最基本的引用类型,它的修改策略是先比较再设值(CAS),所以对它的修改会阻塞直到修改完成
;;每个修改操作都是完全隔离的(自动的),所以是无法协调的
;;使用atom函数产生atom
(def sarah (atom {:name "Sarah" :age 25 :wears-glasses? false}))
;;使用swap!修改atom
(swap! sarah update-in [:age] + 3)
;;swap!返回的是更新之后的值,对atom的修改都是原子的,即使一次使用多个函数对其修改,中间状态也不会被其他线程看到,这避免了ABA问题
(swap! sarah (comp #(update % :age inc)
                   #(assoc % :wears-glasses? true)))
;;swap!是先比较旧的值是否匹配,然后设新的值.如果值在你更新之前发生了变化,那么swap!会自动重试,使用新值来调用传入的函数
(def xs (atom #{1 2 3}))
(common/wait-futures 1 (swap! xs (fn [v]
                                   (Thread/sleep 250)
                                   (println "trying 4")
                                   (conj v 4)))
                     (swap! xs (fn [v]
                                 (Thread/sleep 500)
                                 (println "trying 5")
                                 (conj v 5))))
;trying 4
;trying 5
;trying 5

;;我们无法控制swap!的重试机制,所以传给swap!的函数最好是纯函数
;;swap!会阻塞直到修改完成
(def x (atom 2000))
(swap! x #(Thread/sleep %))

;clojure还提供了compare-and-set!操作,使用它需要直到atom的当前的值是什么.如果更新成功,返回
(compare-and-set! xs :wrong "new value")
;=false
(compare-and-set! xs @xs "new value")
;true
;compare-and-set!要求atom的值和传入的第二个参数必须一样(identical,即是同一个对象)
(def xxs (atom #{1 2}))
(compare-and-set! xxs #{1 2} "new value")
;false

;;如果你不管atom的现在的值而只是想设置一个值,可以使用reset!
(reset! xs :y)

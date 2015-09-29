(ns learnclojure.chapt1.core5
  (:import (java.util Date UUID)))
;;一个简单函数的定义
(fn [x]
  (+ x 10))
;;函数定义的参数和调用函数实际传递的参数之间的定义是通过参数位置定义的
((fn [x]
   (+ x 10)) 8)
;;上面的代码相当于
(let [x 8]
  (+ x 10))
;;多个参数的函数
((fn [x y z]
   (+ x y z))
  3 4 12)
;;等价于
(let [x 3 y 4 z 12]
  (+ x y z))
;;函数可以有多个参数列表
(def strange-adder (fn adder-self-reference
                     ([x] (adder-self-reference x 1))
                     ([x y] (+ x y))))
;;等价于下面的代码，defn是一个封装了def和fn功能的宏，比较常用
(defn strange-adder
  ([x] (strange-adder x 1))
  ([x y] (+ x y)))
(strange-adder 1)
(strange-adder 1 2)
;;letfn可以定义同时定义多个具名函数，并且这些函数可以互相引用
(letfn [(odd? [n]
              (even? (- n 2)))
        (even? [n]
               (or (zero? n)
                   (odd? (- n 2))))]
  (even? 4))
;;可变参函数，下面函数中的rest被称为剩余参数
(defn concat-rest
  [x & rest]
  (apply str (butlast rest)))
(concat-rest 1 2 3 4)
;;剩余参数可以像其他序列那样解构
(defn make-user1
  [& [user-id user-name]]
  {:user-id   (or user-id
                  (str (UUID/randomUUID)))
   :user-name (or user-name
                  (str (rand-int 10)))})
;;(make-user)
(make-user1 "ef2f2a")
;;关键字参数。可以让函数使用者不必按某个特定的顺序传参
(defn make-user2
  [username & {:keys [email join-date]
               :or   {join-date (Date.)}}]
  {:user-name username
   :join-date join-date
   :email     email
   ;;2.592e9 -> one month in ms
   :exp-date  (Date. (long (+ 2.592e9 (.getTime join-date))))
   })
(make-user2 "Zijunjie")
(make-user2 "Zijunjie"
            :join-date (Date. 111 0 1)
            :email "zjjblue@126.com")
;;关键字参数是利用let的map解构的特性实现的，所以关键字的参数名字理论上可以是任何类型的值
;;比如字符串 数字 甚至集合 但最好是用关键字来作为map的key的名字。
(defn foo
  [& {k ["z" 1]}]
  (inc k))
(foo ["z" 1] 5)
(ns learnclojure.ch6.core1
  (:import (clojure.lang IPersistentVector)))

;clojure中接口的对应物就是协议protocol,它有一个或多个方法组成,每个方法至少有一个参数,这个参数相当于java中的this,方法根据这个参数来确定该调用哪个实现
;协议名使用驼峰命名法,因为它最后会被编译为JVM的接口和类,这使得我们容易把协议和clojure中的其他东西区分开
;设计协议时应该面向实现者而不是使用者,一个好的协议是由一些不重复的方法组成的,是容易实现的.
;协议中不能使用解构或剩余参数
;附属函数和工具函数不应该放在协议中
;如果有必要,直接面向用户的函数与协议的方法可以放在不同的命名空间,更清楚的告诉用户哪些是公共的API,哪些是协议是协议实现者的API

(defprotocol Matrix
  "protocol for working with 2D data structures"
  (lookup [matrix i j])
  (update! [matrix i j value])
  (rows [matrix])
  (cols [matrix])
  (dims [matrix]))
;首先扩展vector中是vector的情况
(extend-protocol Matrix
  IPersistentVector ;实现协议的类型
  (lookup [vov i j]
    (get-in vov [i j]))
  (update! [vov i j value]
    (assoc-in vov [i j] value))
  (rows [vov]
    (seq vov))
  (cols [vov]
    (apply map vector vov))
  (dims [vov]
    [(count vov) (count (first vov))]))

;不需要实现所有协议中定义的方法,调用没有实现的方法会简单的抛出异常
;extend-protocol并不是唯一对协议进行扩展的方法,还有:extend,extend-type,内联实现
;;extend-type和extend-protocol是相对的,extend-type用于把多个协议扩展到单个类型,extend-protocol用于把单个协议扩展到多个类型
(defprotocol AProtocol
  (f1 [v]))
(defprotocol BProtocol
  (f2 [v]))
(extend-type IPersistentVector
  AProtocol
  (f1 [v]
    (count v))
  BProtocol
  (f2 [v]
    (reverse v)))
(f1 [1 2])
;2
(f2 [1 2])
;(2 1)

;可以把协议扩展至nil,相当于提供了一个默认实现,这样协议的方法就不会抛出NullPointException
(extend-protocol Matrix
  nil
  (lookup [x i j])
  (update! [x i j value])
  (rows [x] [])
  (cols [x] [])
  (dims [x] [0 0]))
(lookup nil 1 1)
;nil
(dims nil)
;[0 0]

(defn vov
  [h w]
  (vec (repeat h (vec (repeat w nil)))))
(def matrix (vov 3 4))
(def matrix2 (update! matrix 1 1 :x))
(lookup matrix2 1 1)
;:x
(rows matrix2)
;[[nil nil nil nil] [nil :x nil nil] [nil nil nil nil]]
(cols matrix2)
;([nil nil nil] [nil :x nil] [nil nil nil] [nil nil nil])
(dims matrix2)
;[3 4]

;;协议可以扩展Java的类
(extend-protocol Matrix
  (Class/forName "[[D")
  (lookup [matrix i j]
    (aget matrix i j))
  (update! [matrix i j value]
    (let [clone (aclone matrix)]
      (aset clone i
            (doto (aget clone i)
              (aset j value)))
      clone))
  (rows [matrix]
    (map vec matrix))
  (cols [matrix]
    (apply map vector matrix))
  (dims [matrix]
    (let [rs (count matrix)]
      (if (zero? rs)
        [0 0]
        [rs (count (aget matrix 0))]))))
(def matrix3 (make-array Double/TYPE 2 3))
(rows matrix3)
;([0.0 0.0 0.0] [0.0 0.0 0.0])
(def  matrix4 (update! matrix3 1 1 3.3))
(rows matrix3)
;([0.0 0.0 0.0] [0.0 3.3 0.0])
(cols matrix4)
;([0.0 0.0] [0.0 3.3] [0.0 0.0])
(dims matrix4)
;[2 3]

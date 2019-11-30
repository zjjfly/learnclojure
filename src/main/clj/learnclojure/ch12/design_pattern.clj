(ns learnclojure.ch12.design-pattern
  (:import (java.util Map HashMap))
  (:require [robert.hooke :as hooke]))

;clojure的不可变性,动态类型和高阶函数使得很多设计模式不再需要
;看几个常见的设计模式:

;依赖注入
(defprotocol Bark
  (bark [this]))

(defrecord Chihuahua [weight price]
  Bark
  (bark [this] "Yip!"))

(defrecord Mastiff []
  Bark
  (bark [this] "Woof!"))

(defrecord PetStore [dog])

(defn main
  [dog]
  (let [store (PetStore. dog)]
    (println (bark (:dog store)))))

(main (Chihuahua. 1 2))

(main (Mastiff.))

;Java中,PetStore接受的参数局限于实现IDog的接口,但clojure中,只要实现Bark协议的类型都可以
(extend-protocol Bark
  Map
  (bark [this]
    (or (:bark this)
        (get this "bark"))))
;现在可以把任意的Map当做狗来使用
(main (doto (HashMap.)
        (.put "bark" "Ouah!")))
(main {:bark "Wan-wan!"})
;clojure也可以读取配置文件
(defn configured-petstore
  []
  (-> "petstore_config.clj"
      slurp
      read-string
      map->PetStore))
(bark (:dog (configured-petstore)))
;Yip!

;策略模式
(defn quicksort [numbers] numbers)

(defn mergesort [numbers] numbers)

(defn choose-sort
  [performance-first]
  (if performance-first
    quicksort
    mergesort))

;也不是所有的设计模式在clojure中都没用,比如责任链模式
(defn foo
  [data]
  (println "FOO passes")
  true)
(defn bar
  [data]
  (println "BAR" data "and let's stop here")
  false)
(defn baz
  [data]
  (println "BAZ?")
  true)
(defn wrap
  [f1 f2]
  (fn [data]
    (when (f1 data)
      (f2 data))))
;上面的函数的返回值是true表示继续把数据传入链的下一个成员,false表示链结束
(def chain (reduce wrap [foo bar baz]))
(chain "jjzi")

;面向切面编程
;使用robert-hooke这个第三方库可以很方便的给函数加上切面
(defn time-it
  [f & args]
  (let [start (System/currentTimeMillis)]
    (try
      (apply f args)
      (finally
        (println "Run time: " (- (System/currentTimeMillis) start))))))

(defn foo
  [x y]
  (Thread/sleep (rand-int 1000))
  (+ x y))
(hooke/add-hook #'foo time-it)

(foo 1 2)
;Run time: 775
;3

;这个库还提供了暂时或长期禁用或删除钩子的能力
(hooke/with-hooks-disabled foo (foo 1 2))
;3
(hooke/remove-hook #'foo time-it)
(foo 1 2)
;3

;使用这个库配合clojure的内省工具可以很方便的给一个命名空间内的所有函数加上钩子
(doseq [var (->> (ns-publics 'clojure.set)
                 (map val))]
  (hooke/add-hook var time-it))

(clojure.set/intersection (set (range 100000))
                          (set (range -100000 10)))
;Run time:40
;#{0 7 1 4 6 3 2 9 5 8}

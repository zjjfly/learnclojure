(ns learnclojure.ch4.core6
  (:require [learnclojure.ch4.common :as common])
  (:import (java.util ArrayList)))

;;使用校验器来保证数据完整性
;;还是以RPG游戏为例,使用校验器保证治疗角色的时候不会让角色的血量超出他本身最大血量
;;每个角色都有一些公共的校验器
(defn enforce-max-health
  [{:keys [name max-health]}]
  (fn [character-data]
    (or (<= (:health character-data) max-health)
        (throw (IllegalArgumentException. (str name " is already at max health"))))))
(defn heal
  [healer target]
  (dosync
   (let [aid (* (rand 0.1) (:mana @healer))]
     (when (and (pos? aid) (pos? (:mana @healer)))
       (commute healer update :mana - (max 5 (/ aid 5)))
       (commute target update :health + aid)))))
(defn character
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500} opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health cdata) (:validators cdata))]
    (ref (dissoc cdata :validators)
         :validator #(every? (fn [v] (v %)) validators))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))
;(heal gandalf bilbo)
;java.lang.IllegalArgumentException: Bilbo is already at max health
(dosync (alter bilbo assoc :health 95))
;(heal gandalf bilbo)
;java.lang.IllegalArgumentException: Bilbo is already at max health
;原因是目前不支持部分治疗,修改heal方法让其支持
(defn heal
  [healer target]
  (dosync
   (let [aid (min (* (rand 0.1) (:mana @healer))
                  (- (:max-health @target) (:health @target)))]
     (when (and (pos? aid) (pos? (:mana @healer)))
       (commute healer update :mana - (max 5 (/ aid 5)))
       (alter target update :health + aid)))))
(heal gandalf bilbo)

;;STM缺点以及clojure的解决方案:
;;1.不能有副作用,因为事务会重试,这可能会引发问题.一种解决方法是把可能会被误用到事务中的产生副作用的部分事业io!宏包起来
;;所以swap!这样的操作也不能在事务中出现
(defn unsafe
  "docstring"
  []
  (io! (println "writing to database...")))
;(dosync (unsafe))
;java.lang.IllegalStateException: I/O in transaction

;;还要注意ref中放的值也需要是不可变的
;;理论上可以放可变对象,但会出现不可预料的结果
(def x (ref (ArrayList.)))
(common/wait-futures 2 (dosync
                        (dotimes [v 5]
                          (Thread/sleep (rand-int 50))
                          (alter x #(doto % (.add v))))))

@x
;=[0 0 1 2 0 3 4 0 1 2 3 4].有4个0,显然是有问题的

;;2.长事务容易发生冲突,导致不断重试,从而影响性能
;;不要编写长事务的关键是不再事务中进行耗时的计算以及减少事务涉及的ref数量

;;如果系统负载很高,那么长事务有可能会永远提交不了,而是在不断重试,这叫活锁,相当于STM世界的死锁
;;clojure的STM对这个问题的一个解决办法是barging.在某些情况下,当一个老事务和新事务进行竞争的时候,会强迫新事务重试,如果事务重试了一定次数后,系统会让这个事务失败
(def x (ref 0))
;(dosync
;  @(future (dosync (ref-set x 0)))
;  (ref-set x 1))
;java.lang.RuntimeException: Transaction failed after reaching retry limit

;;3.读线程也可能重试
;;如果在一个事务内,用deref对一个ref解引用也可能会引发事务重试
;;原因是如果在事务开始后有其他事务提交了对ref的修改,那么在本事务用deref无法获取到事务开始的时候的ref的值了.
;;STM意识到了这个问题,它会维护事务中ref的一定长度的历史版本值,它的长度会在每次重试的时候递增
;获取当前的ref的历史版本数
(ref-history-count x)
;获取当前的ref的最大历史版本数
(ref-max-history (ref "abc" :max-history 30))
;获取当前的ref的最小历史版本数
(ref-max-history (ref "abc" :min-history 10))

;;deref引发的重试通常出现在只读事务中,这这些事务中,我们想获取对一些活跃修改的ref的快照,下面是一个例子
(def a (ref 0))
(future (dotimes [_ 500]
          (dosync
           (Thread/sleep 200)
           (alter a inc))))
@(future (dosync (Thread/sleep 1000) @a))
;=128,说明读取事务在所有写事务完成之前提交成功了
(ref-history-count a)
;=5

;;如果写事务再快一点
(dosync (ref-set a 0))
(future (dotimes [_ 500]
          (dosync
           (Thread/sleep 20)
           (alter a inc))))
@(future (dosync (Thread/sleep 1000) @a))
;=500,说明读取事务在所有写事务完成之后才提交成功
(ref-history-count a)
;=10,历史版本达到了最大

;;可以把最大历史版本长度调大一点
(def a (ref 0 :max-history 100))
(future (dotimes [_ 500]
          (dosync
           (Thread/sleep 20)
           (alter a inc))))
@(future (dosync (Thread/sleep 1000) @a))
;=500,还是不对
(ref-history-count a)
;=10

;;没有成功的原因是,当有足够多的历史版本的时候,写事务已经完成了,所以关键是要设置一个最小历史版本长度
(def a (ref 0 :max-history 100 :min-history 50))
;选择50作为最小历史版本长度,是因为读事务比写事务慢50倍
(future (dotimes [_ 500]
          (dosync
           (Thread/sleep 20)
           (alter a inc))))
@(future (dosync (Thread/sleep 1000) @a))
;=47,成功!
(ref-history-count a)
;=50,没有重试!

;;4.写偏移(write skew)
;;当前事务对一个ref a的修改依赖于另一个ref b的值,这个事务在过程中读取b,然后计算出a的新值,然后提交,但发现b被另一个事务修改了,那这个写入a的新值就不对了
;;以之前的RPG游戏的attack方法为例,假设攻击的伤害和时间有关,白天的伤害比晚上高
(def daylight (ref 1))
(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor) (ensure daylight))]
     (println damage "," @daylight)
     (commute target update-in [:health] #(max 0 (- % damage))))))
;ensure保证了daylight读取到的daylight是最新的,它在语义上和(alter a identity)和(ref-set a @a)是等效的,但它可以最小化重试的次数

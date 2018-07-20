(ns learnclojure.chapt4.core5
  (:require [learnclojure.chapt4.common :as common]))

;ref是协调引用类型,使用它可以保证多个线程可以交互的对这个ref进行操作
;;ref会一直保持在一个一致的状态,任何条件都不会出现外界可见的不一致状态
;;在多个ref之间不会产生竞态条件
;;不需要使用锁,monitor等底层的同步原语
;;不可能出现死锁
;;实现这些特点都是因为clojure实现了软件事务内存(STM),通过这个机制来协调各个线程对ref的修改
;;STM和数据库中的事务很类似,它相比手动的管理锁来说更加简单,而且通常效果更好

;;STM事务保证对ref的修改是:
;;1.原子的,因此一个事务中对于ref所有的修改要么全部成功,要么全部失败
;;2.一致的,如果对ref设置的新值不满足ref的约束条件,那么这个事务将会失败
;;3.隔离的,一个事务对ref的修改不会影响其他线程内对ref的读取

;;以一个RPG游戏为例子说明ref的用法

(defn character
  "docstring"
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500} opts)))
(def smaug (character "Smaug" :items (set (range 50)) :strength 400))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))

;;所以对ref的修改必须发生在一个事务里,对这些修改的处理都是以同步的形式进行的,也就是启动一个事务的线程在该事务完成之前是阻塞的
;;如果两个事务对一个共享的ref同时进行有冲突的修改,那么其中一个事务需要重试.两个对共享ref进行修改的事务冲突与否,是由使用的修改ref的函数决定的
;;修改ref的函数有:alter,commute,ref-set
(defn loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (alter to update-in [:items] conj item)
     (alter from update-in [:items] disj item))))
;;使用两个线程并发的拾取史矛革的武器
(common/wait-futures 1
                     (while (loot smaug bilbo))
                     (while (loot smaug gandalf)))
;验证正确性
(map (comp count :items deref) [bilbo gandalf])
;=(29 21),说明两个人把史矛革的所有武器都拿走了
(filter (:items @bilbo) (:items @gandalf))
;=(),说明两人没有任何一件相同的武器

;;事务的开始是ref第一次被修改的时候
;;在事务中对ref的修改只是修改了事务内的值,并且对其他事务是不可见的,类似数据库操作的事务.
;;只有当这个事务提交之后才会真正的修改ref中的值,这个时候这个新值是对其他事务是可见的
;;但是如果外部的事务已经提交了对ref的修改,那么本事务会与其发生冲突,这会导致本事务进行重试,使用ref的新值
;;这个过程中,对ref的只读线程不会阻塞

;;对于alter,它的独特之处是:当这个事务提交时,ref当前的值需要和这个事务第一次调用alter时的ref的值一样,否则事务会重新执行

;;可以这样理解STM:它是一个乐观的尝试对并发的修改操作进行重新排序,以使它们可以顺序的执行的一个过程

;;alter虽然最安全,但是如果可以安全的对ref的修改的操作进行重新排序,可以使用commute
;;传给commute的函数需要可以重新排序而不影响程序的语义,一般是符合交换律的函数,但不是绝对的.
(= ((comp #(/ % 3) #(/ % 4)) 120) ((comp #(/ % 4) #(/ % 3)) 120))
;=true

;;commute和alter不同的是,它不会和其他事务冲突(这提高了性能和吞吐量),在最终提交的时候,它会使用最新的ref中的值重新算一遍新值
;;commute的例子:
(def x (ref 0))
;;使用alter
(time (common/wait-futures 5
                           (dotimes [_ 1000]
                             (dosync (alter x + (apply + (range 1000)))))
                           (dotimes [_ 1000]
                             (dosync (alter x - (apply + (range 1000)))))))
;;="Elapsed time: 689.006059 msecs"
;;使用commute
(time (common/wait-futures 5
                           (dotimes [_ 1000]
                             (dosync (commute x + (apply + (range 1000)))))
                           (dotimes [_ 1000]
                             (dosync (commute x - (apply + (range 1000)))))))
;;="Elapsed time: 288.680542 msecs",性能显著提高了

;;但有些情况使用commute会有问题,以之前的RPG游戏为例子,用commute实现loot函数:
(defn flawed-loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (commute to update-in [:items] conj item)
     (commute from update-in [:items] disj item))))
(def smaug (character "Smaug" :items (set (range 50)) :strength 400))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))
(common/wait-futures 1
                     (while (flawed-loot smaug bilbo))
                     (while (flawed-loot smaug gandalf)))
;验证
(map (comp count :items deref) [bilbo gandalf])
;=(45 44),显然不正确
(filter (:items @bilbo) (:items @gandalf))
;=(0 7 20 27 1 24 39 4 15 48 21 31 33 13 36 41 43 44 25 17 3 12 23 47 35 19 11 9 5 14 45 26 38 30 10 18 37 8 49)

;;错误的原因是从史矛革的武器库中删除武器的操作上,删除武器一定要使用alter,因为这个操作如果重新排序,会影响最终结果
(defn fixed-loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (commute to update-in [:items] conj item)
     (alter from update-in [:items] disj item))))
(def smaug (character "Smaug" :items (set (range 50)) :strength 400))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))
(common/wait-futures 1
                     (while (fixed-loot smaug bilbo))
                     (while (fixed-loot smaug gandalf)))
;验证
(map (comp count :items deref) [bilbo gandalf])
;=(25 25),正确
(filter (:items @bilbo) (:items @gandalf))
;=(),正确

;;commute还可以使用与attack和heal这样只对角色某个属性进行修改的操作
(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor))]
     (commute target update :health #(max 0 (- % damage))))))
(defn heal
  [healer target]
  (dosync
   (let [aid (* (rand 0.1) (:mana @healer))]
     (when (and (pos? aid) (pos? (:mana @healer)))
       (commute healer update :mana - (max 5 (/ aid 5)))
       (commute target update :health + aid)))))
;;在加上一些帮助函数
(def alive? (comp pos? :health)) s
(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))
;;接下来让角色进行战斗
(common/wait-futures 1
                     (play bilbo attack smaug)
                     (play smaug attack bilbo))
(map (comp :health deref) [bilbo smaug])
;;重置血量
(dosync
 (alter bilbo assoc :health 100)
 (alter smaug assoc :health  500)
 (alter gandalf assoc :mana  750))
;;让三个角色进行战斗
(common/wait-futures 1
                     (play bilbo attack smaug)
                     (play smaug attack bilbo)
                     (play gandalf heal bilbo))
(map (comp #(select-keys % [:name :health :mana]) deref) [bilbo gandalf smaug])

;;ref-set会把ref设定为一个给定的值,一般用于重新初始化ref到初始状态
;;它的语义和alter一样,如果在这个事务提交之前ref被其他事务修改了,这个事务会重试
(dosync (ref-set bilbo {:name "Bilbo"}))
;相当于
(dosync (alter bilbo (constantly {:name "Bilbo"})))
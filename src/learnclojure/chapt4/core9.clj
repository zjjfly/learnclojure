(ns learnclojure.chapt4.core9
  (:require [clojure.java.io :as io]
            [learnclojure.chapt4.common :as common]
            [learnclojure.chapt4.rpg :as rpg])
  (:import (java.io Writer)))

;agent,和atom和ref不同的是:
;1.对它发起修改的线程和实际对它进行修改的线程不是同一个
;2.可以安全的使用agent进行I/O以及其他具有副作用的操作
;3.agent是STM感知的,因此可以很安全的用在事务重试的场景

;;agent使用send和send-off进行更新,参数和atom以及ref的更新函数的参数是一样的
;;更新函数和参数组成了一个agent action,每个agent会维护一个action的队列
;;调用send和send-off直接会返回,它们只是简单的把action加入action队列,这些action会依照它们发送的顺序在一些专门执行action的线程中串行的执行
;;每个action执行的返回值都会被设置为agent的新状态

;;send和send-off的区别是它们发送给agent的action类型.
;;send发送的action是由一个固定大小的线程池执行的,线程大小是cpu核心数的2倍,这种线程池适合用于cpu密集型的任务
;;send-off发送的action是由一个不限制大小的线程池执行的,这适合IO密集型任务
(def a (agent 500))
(send a range 1000)
@a

;;调用send和send-off会马上返回相关的agent,当在repl中发送action的之后,有可能会看到更新之后的值,这取决于action的复杂度和多快被调用
(def a (agent 0))
(send a inc)
;#object[clojure.lang.Agent 0x75ce54a4 {:status :ready, :val 1}],可以看到是更新后的值

;如果action比较耗时,那么可以使用await等待action执行结束
(def a (agent 5000))
(def b (agent 10000))
(send-off a #(Thread/sleep %))
(send-off b #(Thread/sleep %))
(await a b)
;还有一个await-for函数,作用和await一样,但可以指定一个超时时间
(await-for 10000 a b)
;;获取agent当前队列的中的action的数量
(.getQueueCount b)

;处理action的错误
;;一般情况下,如果action出错了,会让agent默默挂掉,再向它发送action会抛出异常
(def a (agent nil))
(send a (fn [_] (throw (Exception. "something is wrong"))))
a
;#object[clojure.lang.Agent 0x54d23ba2 {:status :failed, :val nil}]
;(send a identity)
;java.lang.RuntimeException: Agent is failed, needs restart

;;可以使用agent-error获取action抛出的异常
(agent-error a)
;error{:cause "something is wrong",
;       :via [{:type java.lang.Exception,
;              :message "something is wrong",
;              :at [learnclojure.chapt4.core9$eval1378$fn__1379 invoke "core9.clj" 39]}],
;       :trace [[....]]}

(agent-error b)
;nil,没有失败的agent调用agent-error返回nil

;status是failed的agent需要重启,使用restart-agent
(restart-agent a 42)
(send a inc)
(reduce send a (for [x (range 3)]
                 (fn [_] (throw (Exception. (str "Error #" x))))))
(agent-error a)
;#error{:cause "Error #0"...
(restart-agent a 50)
(agent-error a)
;#error{:cause "Error #1"...说明之前在队列中的action在restart之后会继续执行
;;如果不想在restart之后执行队列中留存的action,需要在调用restart-agent的时候指定参数:clear-actions为true
(restart-agent a 50 :clear-actions true)
(agent-error a)
;nil

;;默认的错误处理模式是让agent编程失败状态,还有一种是agent继续执行action,并可以继续接受action
;;直接忽略掉异常显然不行,所以一般会指定一个错误处理器,它有两个参数:发生异常的agent以及错误对象
(def a (agent nil
              :error-mode :continue
              :error-handler (fn [the-agent exception]
                               (println (.getMessage exception)))))
(send a (fn [_] (throw (Exception. "something is wrong"))))
;something is wrong
;#object[clojure.lang.Agent 0xa04bacb {:status :ready, :val nil}]

;;可以使用错误处理器做更多事情,比如需要把agent的模式设置回:fail
;;使用set-error-handler!修改agent的错误处理器,使用set-error-mode!设置agent的错误处理模式
(set-error-handler! a (fn [the-agent exception]
                        (when (= "FATAL" (.getMessage exception))
                          (set-error-mode! the-agent :fail))))
(send a (fn [_] (throw (Exception. "FATAL"))))
a
;#object[clojure.lang.Agent 0x2521021d {:status :failed, :val nil}],状态是failed,说明错误处理器生效了

;;agent是任何使用ref或clojure的STM来维持状态的程序中不可缺少的组件,而且由于它的独特语义,使得它成为简化异步I/O操作的理想组件
;;可以用agent保持一个数据库连接,队列或网络socket的OutPutStream,因为action是串行执行的,所以每个action执行的时候都独占这个连接
;;agent可以用在STM事务中,因为在事务内提交的action只有当这个事务成功提交之后才会执行

;;实现一个角色状态变化记录器
(def console (agent *out*))
(def character-log (agent (io/writer "character-state.log" :append true)))
(defn write
  [^Writer w & content]
  (doseq [x (interpose " " content)]
    (.write w (str x)))
  ;;doto的返回值的是第一个参数,所以在这里比较合适
  (doto w
    (.write "\n")
    (.flush)))
(defn log-ref
  [reference & writer-agents]
  (add-watch reference :log
             (fn [_ reference old new]
               (doseq [writer-agent writer-agents]
                 (send-off writer-agent write new)))))

(def smaug (rpg/character "Smaug" :items (set (range 50)) :strength 400))
(def bilbo (rpg/character "Bilbo" :health 100 :strength 100))
(def gandalf (rpg/character "Gandalf" :health 75 :mana 750))
(log-ref smaug console character-log)
(log-ref bilbo console character-log)
(common/wait-futures 1
                     (rpg/play smaug rpg/attack bilbo)
                     (rpg/play bilbo rpg/attack smaug)
                     (rpg/play gandalf rpg/heal bilbo))

;;记录攻击和治疗信息
(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor) (ensure rpg/daylight))]
     (send-off console write
               (:name @aggressor) "hit" (:name @target) "for" damage)
     (commute target update-in [:health] #(max 0 (- % damage))))))
(defn heal
  [healer target]
  (dosync
   (let [aid (min (* (rand 0.1) (:mana @healer))
                  (- (:max-health @target) (:health @target)))]
     (when (and (pos? aid) (pos? (:mana @healer)))
       (send-off console write
                 (:name @healer) "heals" (:name @target) "for" aid)
       (commute healer update :mana - (max 5 (/ aid 5)))
       (alter target update :health + aid)))))
(dosync
 (alter bilbo assoc :health 100)
 (alter smaug assoc :health 500)
 (alter gandalf assoc :mana 750))

(common/wait-futures 1
                     (rpg/play smaug attack bilbo)
                     (rpg/play bilbo attack smaug)
                     (rpg/play gandalf heal bilbo))
;(send-off character-log #(.close (deref %)))
;(restart-agent character-log (agent (io/writer "character-state.log" :append true)))



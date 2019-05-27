(ns learnclojure.ch7.core3)

;type,和class类似,除非对象有:type的元数据,否则返回和class一样
(class {})
;clojure.lang.PersistentArrayMap
(type {})
;clojure.lang.PersistentArrayMap
(type ^{:type :a-tag} {})
;:a-tag

;由于自身的这个特性,它可以作为一种简单的把数据分为不同类别并让它们用于多重方法的方法
(defmulti run "Executes the computation" type)
(defmethod run Runnable
  [x]
  (do (.run x)
      (println "run")))
(defmethod run Callable
  [x]
  (do (.call x)
      (println "call")))
(prefer-method run Callable Runnable)
(defmethod run :runnable-map
  [m]
  (run (:run m)))
(run #(print "hello!"))
(run (reify Runnable
       (run [_] (print "hello!"))))
(run ^{:type :runnable-map}
     {:run #(print "hello!") :other :data})


;之前的转发函数都只依赖参数的返回值,但其实没有这种限制
(def prioripties (atom {:911-call :high
                        :evacuation :high
                        :pothole-report :low
                        :tree-down :low
                        }))
(defmulti route-message
          (fn [message]
            ((:type message) @prioripties)))
(defmethod route-message :high
  [{:keys [type]}]
  (println (format "Alter the authorities,this is a %s!" (name type))))
(defmethod route-message :low
  [{:keys [type]}]
  (println (format "Oh,there is another %s.Put it in the log" (name type))))
(route-message {:type :911-call})
(route-message {:type :tree-down})
;如果想改变route-message的行为,可以直接修改prioripties
(swap! prioripties assoc :tree-down :high)

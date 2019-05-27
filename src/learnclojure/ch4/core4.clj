(ns learnclojure.ch4.core4)

;;两个所有引用类型都有的特性:观察器和校验器

;;引用类型一开始是没有观察器的,可以在任意的时候给它们注册一个或移除一个观察器
;;一个观察器就是一个函数,它有四个参数:key(注册观察器的时候的指定的key),发生变化的引用,引用的旧状态和新状态
(defn echo-watch
  [key identity old new]
  (println key old "=>" new))
(def sarah (atom {:name "Sarah" :age 25}))
(add-watch sarah :echo echo-watch)
(swap! sarah update :age inc)
;:echo {:name Sarah, :age 25} => {:name Sarah, :age 26}
(add-watch sarah :echo2 echo-watch)
(swap! sarah update :age inc)
;:echo {:name Sarah, :age 26} => {:name Sarah, :age 27}
;:echo2 {:name Sarah, :age 26} => {:name Sarah, :age 27}

;;观察器是在修改这个引用的线程中同步调用的,而此时另一个线程可能也对这个引用做了修改
;;所以观察器中不应该去手动对引用进行解引用,而是使用传入的新值和旧值

;;使用add-watch的时候传入的key,可以移除观察器
(remove-watch sarah :echo2)
(swap! sarah update :age inc)
;:echo {:name Sarah, :age 27} => {:name Sarah, :age 28}

;;这些观察器保证在发生修改的时候都会调用,但不保证旧值和新值是不一样的
(reset! sarah @sarah)
;;所以很多时候需要在代码中对新值和旧值做对比,确定它们不一样之后再做一些处理

;;用检查器可以很方便的使得本地修改可以及时的通知到其他引用或者其他系统来说很方便
;;试着使用检查器来实现一个记录引用的历史状态的功能
(def history (atom ()))
(defn log->list
  [dest-atom key source old new]
  (when (not= new old)
    (swap! dest-atom conj new)))
(add-watch sarah :record (partial log->list history))
(swap! sarah update :age inc)
(swap! sarah update :age inc)
@history
;=({:name "Sarah", :age 30} {:name "Sarah", :age 29})
(swap! sarah identity)
@history
;=({:name "Sarah", :age 30} {:name "Sarah", :age 29})

;;校验器,也是一个predicate函数,接受一个参数:修改的新值.
;;如果这个函数返回的是false,对引用类型的修改会失败并抛出异常
(def n (atom 1 :validator pos?))
(swap! n + 500)
;(swap! n - 1000)
;java.lang.IllegalStateException: Invalid reference state

;虽然所以引用类型都可以添加校验器,但是只有atom,ref,agent可以在创建的时候通过:validator直接指定一个校验器.
;如果需要给var添加校验器或者要改变atom,ref,agent的校验器,使用set-validator!
(set-validator! sarah :age)
;(swap! sarah dissoc :age)
;java.lang.IllegalStateException: Invalid reference state

;可以自定义异常来让我们知道为什么修改失败
(set-validator! sarah #(or (:age %)
                           (throw (IllegalArgumentException. "People must have `:age`s!"))))
;(swap! sarah dissoc :age)
;java.lang.IllegalArgumentException: People must have `:age`s!

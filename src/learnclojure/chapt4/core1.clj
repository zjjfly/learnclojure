(ns learnclojure.chapt4.core1)

;;delay,它对其包含的代码只在第一次解引用的时候执行一次,然后把返回值缓存起来,之后对其解引用都是直接返回结果,和Scala的lazy类似
(def d (delay (println "Running...")
              :done))
;;得到delay的运行结果,解引用
(deref d)
;;一般不使用deref,除非需要在高阶函数中使用解引用或者想要在解引用的时候指定超时时间
;;一般使用@这个语法糖
@d

;;如果需要提供一个计算比较费时或者非必须的数据,delay可以作为一种优化手段,类似Scala的lazy
(defn get-document
  [id]
  {:url     "https://www.mozilla.org/en-US/about/manifesto"
   :title   "The Mozilla Manifesto"
   :mime    "text/html"
   :content (delay (slurp "https://www.mozilla.org/en-US/about/manifesto"))})
(def doc (get-document "some-id"))
;;使用realized?来检测delay是否已经获取到,它也可以用于promise和future
(realized? (:content doc))
@(:content doc)
;=false
(realized? (:content doc))
;=true

;;future
(def long-calculation (future (apply + (range 1e8))))
@long-calculation
;;和delay一样,在future还没有完成的时候解引用会阻塞当前线程
@(future (Thread/sleep 3000) :done!)
;;另一点和delay一样的是,future也会保存代码的返回值,后续对其的解引用会直接返回结果
;;和delay不一样的是,使用deref对future解引用的时候可以指定一个超时时间和超时值(它在超时的情况下作为备用的返回值)
;;并且future在第一次调用deref/@的时候可能已经完成了,而delay是在第一次解引用的时候才去执行代码
(deref (future (Thread/sleep 3000) :done!) 1000 :impatient!)
;;future一般用于简化程序的并发部分
(defn get-document2
  [id]
  {:url     "https://www.mozilla.org/en-US/about/manifesto"
   :title   "The Mozilla Manifesto"
   :mime    "text/html"
   :content (future (slurp "https://www.mozilla.org/en-US/about/manifesto"))})
;;future使用的是和agent共享的一个线程池,所以比创建原生线程更高效,语法也更简洁,产生的是类java.util.concurrent.Future的对象,可以很好的和Java代码交互

;;promise
;;promise也可以解引用,并且指定一个超时参数.解引用的时候如果这个promise的值还没有,那么会阻塞直到这个值准备好
;;但它和future,delay不同的是,它在生成的时候不会指定任何的代码来产生最终的结果
(def p (promise))
(realized? p)
;=false
;;通过deliver函数为promise填充值
(deliver p 42)
(realized? p)
;=true

;;promise可以做为声明式并发编程的基础构件
(def a (promise))
(def b (promise))
(def c (promise))
(future
  (deliver c (+ @a @b))
  (println "Delivery complete!"))
(deliver a 15)
(deliver b 16)
@c

;;promise不会检查循环依赖,但还是可以通过手动解锁
(def p1 (promise))
(def p2 (promise))
(future (deliver p1 @p2))
(future (deliver p2 @p1))
;;这个时候如果去对p1或p2解引用的话,会无限阻塞
;;使用下面的代码可以解除无限阻塞
(deliver p1 23)
@p1
@p2

;;promise最直接的应用是让基于回调函数的API以同步的方式执行
(defn call-service
  [arg1 arg2 callback-fn]
  (future (callback-fn (+ arg1 arg2) (- arg1 arg2))))
;;假设传入的异步函数的最后一个参数就是回调函数
(defn sync-fn
  [async-fn]
  (fn [& args]
    (let [result (promise)
          [f & rests] (reverse args)]
      (apply async-fn (conj (vec (reverse rests)) #(deliver result (apply f %&))))
      @result)))
((sync-fn call-service) 8 7 -)
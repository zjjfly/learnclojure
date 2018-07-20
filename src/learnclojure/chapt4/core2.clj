(ns learnclojure.chapt4.core2)

;;简单的并行化
(defn phone-number
  "docstring"
  [string]
  (re-seq #"(\d{3})[\.-]?(\d{3})[\.-]?(\d{4})" string))
(phone-number "Sunil:717.555.2311,Betty:241.120.2313")
;;假设有100个文件,每个文件中都有若干电话号码,需要取出这些电话号码
(def files
  (repeat 100
          (apply str
                 (concat (repeat 1000000 \space)
                         "Sunil:717.555.2311,Betty:241.120.2313"))))
(time (dorun (map phone-number files)))
;;使用pmap来并行化获取文件的电话号码操作
(time (dorun (pmap phone-number files)))
(future (phone-number (concat (repeat 100000 \space)
                 "Sunil:717.555.2311,Betty:241.120.2313")))
;;注意,pmap会有一些额外的开销,只适用于耗时的CPU密集型计算,对于耗时很短的操作,效率可能反而没有mapgao

;;把任务数量增多但单个任务的耗时变小
(def files1
  (repeat 10000
          (apply str
                 (concat (repeat 1000 \space)
                         "Sunil:717.555.2311,Betty:241.120.2313"))))
(time (dorun (map phone-number files1)))
(time (dorun (pmap phone-number files1)))
;;对于这种情况,可以把小任务合并成一个大任务
(time (->> files1
           (partition 250)
           (pmap #(doall (map phone-number %)))
           (apply concat)
           #_("dorun可以阻止repl打印解析出来的map") dorun))

;;clojure还有另外两个基于pmap的pcalls和pvalues
;;pcalls接受任意数量的无参函数,返回包含它们的返回值的惰性序列
(apply pcalls (repeat 10 #(rand-int 10)))
;;pvalues是一个macro,和pcalls类似,但它的参数是任意数量的表达式
(pvalues (rand-int 10) (rand-int 10) (rand-int 10))
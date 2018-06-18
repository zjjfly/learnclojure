(ns learnclojure.chapt3.core8)
;;有时候我们不需要把元素分组,而是要计算聚合信息,我们可以用group-by来分组,然后对每个元素进行处理来计算聚合信息.
;;(into {} (for [[k v] (group-by key-fn coll)]
;;           [k (summarize v)]))
;;这里的key-fn和summarize都需要自己实现,但如果集合很大的话,这样写代码会很笨重
;;这种情况下,我们可以利用group-by和reduce来定义一个函数
;;reduce-by可以用来对任意的数据计算聚合信息,和sql中的select...group by...作用差不多.
(defn reduce-by
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
          {} coll))
;;其中,key-fn类似于group by后面的字段,f是聚合函数,init是聚合函数的初始值,coll是集合
;;在clojure中,x,xs并不是很糟糕的命名,这样命名的意思是说明这段代码是很通用的.
;;而且x的类型对于代码阅读者应该是很明显的,所以没必要命名成person这样的名字,类似的,包含x的集合可以命名成xs.

;;假设有一些订单数据,我们利用map来表示
(def orders
  [{:product "Clock", :customer "Wile Coyote", :qty 6, :total 300}
   {:product "Dynamite", :customer "Wile Coyote", :qty 20, :total 5000}
   {:product "Shotgun", :customer "Elmer Fudd", :qty 2, :total 800}
   {:product "Shells", :customer "Elmer Fudd", :qty 4, :total 100}
   {:product "Hole", :customer "Wile Coyote", :qty 1, :total 1000}
   {:product "Anvil", :customer "Elmer Fudd", :qty 2, :total 300}
   {:product "Anvil", :customer "Wile Coyote", :qty 6, :total 900}])
;;利用reduce-by,可以很方便的算出每个人的订单总金额
(reduce-by :customer #(+ %1 (:total %2)) 0 orders)
;;计算每件商品的销量
(reduce-by :product #(+ %1 (:qty %2)) 0 orders)
;;订购每种商品的所以客户的名字
(reduce-by :product #(conj %1 (:customer %2)) [] orders)

;;如果我们需要两个维度分组呢?比如像查看每个客户在每种商品上花了多少钱,只需要返回一个包含两个值的vector作为key函数.
;;有很多种方法编写这个函数
(fn [order]
  [(:customer order) (:product order)])
#(vector (:customer %) (:product %))
(fn [{:keys [customer product]}]
  [customer product])
;;最清晰简洁的一种:
(juxt :customer :product)
(reduce-by (juxt :customer :product) #(+ %1 (:total %2)) 0 orders)
;={["Wile Coyote" "Clock"] 300,
;["Wile Coyote" "Dynamite"] 5000,
;["Elmer Fudd" "Shotgun"] 800,
;["Elmer Fudd" "Shells"] 100,
;["Wile Coyote" "Hole"] 1000,
;["Elmer Fudd" "Anvil"] 300,
;["Wile Coyote" "Anvil"] 900}
;;这个结果和我们预期的有些差距,我们想要的可能是一个包含map的map
;;这和reduce-by的实现有关,它假设map是一维的.
;;要让reduce-by返回嵌套的map很简单,只要把assoc以及隐式调用get的地方全部换成assoc-in和get-in就行了.
(defn reduce-by-in
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [ks (key-fn x)]
              (assoc-in summaries ks
                        (f (get-in summaries ks init) x))))
          {} coll))
(reduce-by-in (juxt :customer :product)
              #(+ %1 (:total %2)) 0 orders)
;;{"Wile Coyote" {"Clock" 300, "Dynamite" 5000, "Hole" 1000, "Anvil" 900},
;;"Elmer Fudd" {"Shotgun" 800, "Shells" 100, "Anvil" 300}}

;;还有一个办法是对reduce-by的结果做转换
(def flat-breakup
  (reduce-by (juxt :customer :product) #(+ %1 (:total %2)) 0 orders))
(reduce #(apply assoc-in %1 %2) {} flat-breakup)
;;由flat-breakup提供的序列的每一个值被当做map的一个元素(entry)
;;比如[["Wile Coyote" "Anvil"] 900],当reduce调用apply对map的每一个元素进行处理的时候,对每个元素调用的函数其实是assoc-in.
;;比如(assoc-in {} ["Wile Coyote" "Anvil"] 900),我们使用["Wile Coyote" "Anvil"]来定义map的key,900来定义它的值
(update {} :a + 1)
(ns learnclojure.ch7.core2
  (:import (clojure.lang IPersistentSet)))

(def fill-hierarchy (-> (make-hierarchy)
                        (derive :input.radio ::checkable)
                        (derive :input.checkbox ::checkable)
                        (derive ::checkable :input)
                        (derive :input.txt :input)
                        (derive :input.hidden :input)))
(defn- fill-dispatch [node value]
  (if-let [type (and (= :input (:tag node))
                     (-> node :attrs :type))]
    (keyword (str "input." type))
    (:tag node)))
(defmulti fill
  "Fill a xml/html node (as per clojure.xml)
          with provide value"
  #'fill-dispatch
  :default nil
  :hierarchy #'fill-hierarchy) ;:hierarchy的数值是一个层级的引用
(defmethod fill nil
  [node value]
  (if (= :input (:tag node))
    (do
      (alter-var-root #'fill-hierarchy
                      derive (fill-dispatch node value) :input)
      (fill node value))
    (assoc node :content [(str value)])))
(defmethod fill :input
  [node value]
  (assoc-in node [:attrs :value] (str value)))
(defmethod fill ::checkable
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))

(fill {:tag :input
       :attrs {:type "date"}}
      "20110820")

;isa?的参数可以是vector,会一个一个的进行判断
(isa? fill-hierarchy [:input.checkbox :input.txt] [::checkable :input])
;利用这一点来重写fill-dispatch
(ns-unmap *ns* 'fill)

(def fill-hierarchy (-> (make-hierarchy)
                        (derive :input.radio ::checkable)
                        (derive :input.checkbox ::checkable)))
(defn- fill-dispatch [node value]
  (if-let [type (and (= :input (:tag node))
                     (-> node :attrs :type))]
    [(keyword (str "input." type)) (class value)]
    [(:tag node) (class value)]))
(defmulti fill
  #'fill-dispatch
  :default nil
  :hierarchy #'fill-hierarchy)

(defmethod fill nil
  [node value]
  (if (= :input (:tag node))
    (do
      (alter-var-root #'fill-hierarchy
                      derive (first (fill-dispatch node value)) :input)
      (fill node value))
    (assoc node :content [(str value)])))
(defmethod fill [:input Object]
  [node value]
  (assoc-in node [:attrs :value] (str value)))
(defmethod fill [::checkable IPersistentSet]
  [node value]
  (if (contains? value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))
(fill {:tag :input
       :attrs {:type "checkbox"
               :value "yes"}}
      #{"yes" "y"})
;(fill *1 #{"no" "n"})
(fill {:tag :input
       :attrs {:type "text"}}
      "some text")
(fill {:tag :h1} "Big Title")

;多重继承
(defmulti run "Executes the computation" class)
(defmethod run Runnable
  [x]
  (do (.run x)
      (println "run")))
(defmethod run Callable
  [x]
  (do (.call x)
      (println "call")))
;(run #(println "hehe"))
;java.lang.IllegalArgumentException: Multiple methods in multimethod 'run' match dispatch value

;clojure的函数同时实现了Runnable和Callable,所以不知道使用哪一个派发
;这种时候需要使用prefer-method
(prefer-method run Callable Runnable)
(run #(print "Hello,"))
;Hello,call

;有一些方法可以对多重方法进行元编程
(println (methods run))
(println (get-method run Callable))
(remove-method run Callable)
(run #(print "Hello,"))
;Hello,run
(remove-all-methods run)

;为什么没有add-method?可以通过展开defmethod来找到实现方法
(macroexpand-1 '(defmethod mmethod-name dispatch-value [arg] body))
;(. mmethod-name clojure.core/addMethod dispatch-value (clojure.core/fn [arg] body))
(defn add-method [multifn dispatch-val f]
  (.addMethod multifn dispatch-val f))
(add-method run Callable (fn [x] (do (.call x)
                                     (println "call"))))
(run #(print "Hello,"))



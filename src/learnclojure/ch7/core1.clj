(ns learnclojure.ch7.core1)
;多重方法
;先声明一个多重方法的转发函数
;调用多重方法的时候先经过这个转发函数,通过它的返回值确定要调用的实现函数
(defmulti fill
  (fn [node value] (:tag node)))
;再声明方法的实现
(defmethod fill :div
  [node value]
  (assoc node :content [(str value)]))
(defmethod fill :input
  [node value]
  (assoc-in node [:attrs :value] (str value)))
(fill {:tag :div} "hello")
(fill {:tag :input} "hello")
;下面的代码会报错,因为没有对应的实现方法
;(fill {:span :input} "hello")
;IllegalArgumentException: No method in multimethod 'fill' for dispatch value: null,
;可以使用默认转发值:default来解决这个问题
(defmethod fill :default
  [node value]
  (assoc node :content [(str value)]))
(fill {:span :input} "hello")
;但如果是转发函数的返回值是:default,那么就会有歧义,所幸defmulti可以让用户指定默认转发值
(defmulti fill
  (fn [node value] (:tag node))
  :default nil)
;使用新的默认转发值声明默认实现
(defmethod fill nil
  [node value]
  (assoc node :content [(str value)]))

;先清除当前空间之中对fill的映射
;这么做的原因是:如果不先清除则对多重方法的重新定义会被忽略
(ns-unmap *ns* 'fill)

(defn- fill-dispatch [node value]
  (if (= :input (:tag node))
    [(:tag node) (-> node :attrs :type)]
    (:tag node)))

;在fill-dispatch之前加#'而不是直接使用fill-dispatch,这样就添加一层重定向,让我们可以在运行时动态的修改这个转发函数的行为
(defmulti fill
  #'fill-dispatch
  :default nil)

(defmethod fill nil
  [node value]
  (assoc node :content [(str value)]))

(defmethod fill [:input nil]
  [node value]
  (assoc node [:attrs :value] [(str value)]))

(defmethod fill [:input "hidden"]
  [node value]
  (assoc node [:attrs :value] [(str value)]))

(defmethod fill [:input "text"]
  [node value]
  (assoc node [:attrs :value] [(str value)]))

(defmethod fill [:input "radio"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))

(defmethod fill [:input "checkbox"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))

(fill {:tag :input :attrs {:type "checkbox" :value "first choice"}} "first choice")
;{:tag :input, :attrs {:type "checkbox", :value "first choice", :checked "checked"}}
(fill *1 "off")

;为了能一次性对一类具有共同特点的类型声明多重方法的实现,需要使用层级
;一般的层次是命名空间限定的,所以使用::keyword,"::"表示是当前命名空间的关键字
(derive ::checkbox ::checkable)
(derive ::radio ::checkable)
(derive ::checkable ::input)
(derive ::text ::input)
;使用isa?来测试两个关键字是否有层级关系
(isa? ::radio ::input)
;true
(isa? ::radio ::text)
;false
;isa?不常用,如果你用的很多说明你需要提取一个多重方法出来了
;isa?可以用于类型和接口
(isa? java.util.ArrayList Object)
;true
(isa? java.util.ArrayList java.util.List)
;true
(isa? java.util.ArrayList java.util.Map)
;false

;其他类似isa?的自省方法
;移除两个关键字之间的层级关系
(underive ::t ::input)
;获取关键字的子级,不能用于Java类型
(descendants ::input)
;ancestors获取传入的关键字或类型的的父级,包括直接和间接的
(ancestors clojure.lang.PersistentHashSet)
;parents获取传入的关键字或类型的的直接父级
(parents ::checkbox)

;类和接口也可以用于clojure层级中,但只能声明为子级
(derive java.util.Map ::collection)
(derive java.util.Collection ::collection)
(isa? java.util.ArrayList ::collection)
;true
(isa? java.util.HashMap ::collection)
;true

;整个Java类层次总是每个层级的一部分,即使这个层级是刚用make-hierarchy创建的
(def h (make-hierarchy))
(isa? h java.util.ArrayList java.util.Collection)

(ns learnclojure.ch5.core2)

;;宏卫生问题,就是宏中使用的符号可能在当前命名空间中已经绑定一个值了,这就产生了问题
(defmacro unhygienic
  [& body]
  `(let [x :oops]
     ~@body))
;(unhygienic (println "x:" x))
;java.lang.RuntimeException: Can't let qualified name: main.clojure.main.clj.learnclojure.ch5.core2/x
(macroexpand-1 '(unhygienic (println "x:" x)))
;(clojure.core/let [main.clojure.main.clj.learnclojure.ch5.core2/x :oops] (println "x:" x)),因为使用了语法引述,所以x被加上了命名空间,这是不对的
;;使用反引述修正这个问题
(defmacro still-unhygienic
  [& body]
  `(let [~'x :oops]
     ~@body))
;但这个宏还是不卫生,因为它可能会和外部的符号名冲突
(let [x :this-is-important]
  (still-unhygienic (println "x:" x)))
;x: :oops,把开头定义的x覆盖了,这可能会引发问题

;;为了解决命名冲突问题,clojure提供了一个函数:gensym,它可以保证返回的符号是唯一的
(gensym)
;它可以接受一个参数作为生产的符号的前缀
(gensym "sym")
;使用它来编写卫生宏
(defmacro hygienic
  [& body]
  (let [sym (gensym)]
    `(let [~sym :macro-value]
       ~@body)))
(macroexpand-1 '(hygienic (println "x:" x)))
(let [x :important-value]
  (hygienic (println "x:" x)))
;import-value,正确

;;gensym在宏中太常用了,所以clojure提供了一个简写形式.在语法引述中以#结尾的符号h会被自动展开为一个自动生成的符号,#之前的作为前缀
(defmacro hygienic
  [& body]
  `(let [x# :macro-value]
     ~@body))
(let [x :important-value]
  (hygienic (println "x:" x)))
;import-value,正确
;对于相同前缀的符号,会被展开成相同的符号名称,这个被称为"自动gensym",这使得我们可以在一个语法引述中对一个gensym的符号多次引用,读起来写起来都很自然
`(x# x#)
;(x__1340__auto__ x__1340__auto__),两个符号相同,正确.
(defmacro auto-gensyms
  [& numbers]
  `(let [x# (rand-int 10)]
     (+ x# ~@numbers)))
(auto-gensyms 1 2)

;;但gensym只能在一个语法引述中保证产生的符号是相同的,如果是下面这种情况就不行
(defmacro out-doto
  [expr & forms]
  `(let [obj# ~expr]
     ~@(map (fn [[f & args]]
              `(~f obj# ~@args)) forms)
     obj#))
;(out-doto "It works"
;          (println "I can't believe it"))
;java.lang.RuntimeException: Unable to resolve symbol: obj__1316__auto__ in this context
;;还是和之前一样直接使用gensym,在开头把它绑定到一个符号
(defmacro out-doto
  [expr & forms]
  (let [obj (gensym "obj")]
    `(let [~obj ~expr]
       ~@(map (fn [[f & args]]
                `(~f ~obj ~@args)) forms)
       ~obj)))
(out-doto "It works"
          (println "I can't believe it")
          (println "I still  can't believe it"))

;如果一个宏向外部暴露出一个名字,这个宏被称为不稳定的宏
;让用户来选择宏中的绑定的名字,这样比在宏中随意定义一个名字要好
(defmacro with
  [name & body]
  `(let [~name 5]
     ~@body))
(with bar (+ bar 4))
(with foo (+ foo 10))

;;如果宏中需要对一个参数进行多次求值,而这个参数不是常量,而是一个方法调用,且每次调用得到结果是不一样的或者调用很耗时,就会产生问题
(defmacro spy [x]
  `(do
     (println "spied" '~x ~x)
     ~x))
(spy 12)
;spied 12 12
;12
(spy (rand-int 10))
;spied (rand-int 10) 1
;2
;;解决方法是,可以引入一个本地绑定
(defmacro spy [x]
  `(let [x# ~x]
     (println "spied" '~x x#)
     x#))
(spy (rand-int 10))
;spied (rand-int 10) 8
;8
;;但这种方法只能算是一种trick,说明你把一些应该在函数中实现的逻辑写在了宏中
;;使用方法&宏重新写
(defn spy-help
  [expr value]
  (println expr value)
  value)
(defmacro spy [x]
  `(spy-help '~x ~x))
(spy (rand-int 10))

(ns learnclojure.ch5.core4
  (:use [learnclojure.ch5.core3 :only [simplify]]))

(defn macroexpand1-env
  [env form]
  (if-let [[x & xs] (and (seq? form) (seq form))]
    (if-let [v (and (symbol? x) (resolve x))]
      (if (-> v meta :macro)
        (apply @v form env xs)
        form)
      form)
    form))
(macroexpand1-env '{} '(simplify (range 10)))
(macroexpand1-env '{range nil} '(simplify (range 10)))
(defmacro spy [expr]
  `(let [value# ~expr]
     (println (str "line #" ~(-> &form meta :line) ",")
              '~expr value#)
     value#))
(let [a 1
      a (spy (inc a))
      a (spy (inc a))]
  a)
;;如果想要执行这个宏的情况下验证它的行号的正确性,可以这么写:
(macroexpand1-env '{} (with-meta '(spy (+ 1 1)) {:line 42}))
;(clojure.core/let
;  [value__1726__auto__ (+ 1 1)]
;  (clojure.core/println (clojure.core/str "line #" 42 ",") (quote (+ 1 1)) value__1726__auto__)
;  value__1726__auto__)
;;使用宏把简化多个嵌套的if-let
(defmacro if-let-all
  [bindings then else]
  (reduce (fn [subform binding]
            `(if-let [~@binding] ~subform ~else))
          then (reverse (partition 2 bindings))))
(defn macroexpand1-env
  [env form]
  (if-let-all [[x & xs] (and (seq? form) (seq form))
               v (and (symbol? x) (resolve x))
               _ (-> v meta :macro)]
              (apply @v form env xs)
              form))

;;自己实现->
(defn ensure-seq
  [x]
  (if (seq? x)
    x
    (list x)))
(defn insert-second
  [x ys]
  (let [ys (ensure-seq ys)]
    (list* (first ys) x (rest ys))))
(defmacro thread
  ([x] x)
  ([x form] (insert-second x form))
  ([x form & more]
   `(thread (thread ~x ~form) ~@more)))
;自己实现->>
(defn insert-last
  [x ys]
  (let [ys (ensure-seq ys)]
    (reverse (list* x (reverse ys)))))
(defmacro ->>'
  ([x] x)
  ([x form] (insert-last x form))
  ([x form & more]
   `(->>' (->> ~x ~form) ~@more)))
(->>' [1 2 3] (concat [2 3]) count inc)
;;使用函数实现->
(defn thread-fns
  ([x] x)
  ([x form] (form x))
  ([x form & more]
   (apply thread-fns (form x) more)))
(thread-fns [1 2 3] reverse #(conj % 4) prn)
;;这个实现不支持Java方法的调用,而用宏实现的那个版本支持
(thread [1 2 3] .toString (.split " ") seq)
;(thread-fns [1 2 3] .toString (.split " ") seq)
;java.lang.RuntimeException: Unable to resolve symbol: .toString in this context

;;clojure标准库中的一些其他串行宏
;;..,只支持对Java方法的调用,是在->之前引入的,现在很少用了
(.. System (getProperties) (get "os.name"))

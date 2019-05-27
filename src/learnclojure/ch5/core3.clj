(ns learnclojure.ch5.core3
  (:require [clojure.java.io :as jio])
  (:import (java.io FileInputStream)))

;;写出更clojure的宏的一些原则
;;1.如果宏本身需要指定本地绑定,那么把绑定放在一个vector中,并且这个vector需要是第一个参数,这样和clojure内置的宏和函数的风格一致
;;看一些内置宏
(let [a 42
      b "abc"]
  (println a b))
(if-let [x (first [1])]
  (println x)
  (println "x no found"))
(with-open [in (jio/reader (FileInputStream. "character-state.log"))]
  (.read in))
;;for相当于scala的for(...) yield ...
(for [x (range 10)
      y (range 5)]
  [x y])

;;2.定义var的时候不要耍小聪明,需要做到:以def开头(和defn defmacro一致),把var的名字作为第一个参数,每个宏只定义一个var(除非需要在宏内部定义一个私有var)
;;3.不要在宏中实现复杂逻辑,宏应该只是在函数的基础上薄薄地封装一层

;;宏的隐藏参数:&env和&form,它们是defmacro的两个参数
;;&env是一个map,它的key是当前上下文中所有本地绑定的名字,这个对于调试很有用
(defmacro spy-env
  []
  (let [ks (keys &env)]
    `(prn (zipmap '~ks [~@ks]))))
(let [x 1
      y 2]
  (spy-env)
  (+ x y))
;{x 1,y 2}
;3

;;&env的另一个作用是在编译器安全的对表达式进行优化,如对没有引用任何本地绑定的表达式进行提前求值,优化函数的性能
(defmacro simplify
  [expr]
  (let [locals (set (keys &env))]
    (if (some locals (flatten expr))
      expr
      (do
        (println "Precomputing:" expr)
        (list `quote (eval expr))))))
(defn f
  [a b c]
  (+ a b c (simplify (apply + (range 5e7)))))
(f 1 2 3)
;1249999975000006
(time (f 1 2 3))
;"Elapsed time: 0.02401 msecs"
;;下面的函数无法优化,因为表达式中引用了本地绑定
(defn f'
  [a b c]
  (simplify (+ a b c (apply + (range 5e7)))))
(time (f' 1 2 3))
;"Elapsed time: 1072.089854 msecs"

;;目前clojure的宏实现成函数,但clojure为了避免一些问题不让我们像使用函数那么用,所以如果需要调试使用&env的宏,需要直接使用实现宏的那个函数
(@#'simplify nil {} '(inc 1)) ;@#'是解引用的一个语法糖,等价于(deref (var simplify)),用于获取私有var的值
;Precomputing: (inc 1)
(@#'simplify nil {'x nil} '(inc x))
;(inc x)

;&form保存的是所有用户指定的元数据,包括类型提示,由reader添加的元数据,比如调用宏的那行代码的行号
;它的一个关键应用是提供准确而有用的信息
;;假设定义一个宏,这个宏接受不定参数,每个参数需要是含有三个元素的集合,否则抛出异常
(defmacro ontology
  [& triples]
  (every? #(or (== 3 (count %))
               (throw (IllegalArgumentException. (format "`%s` provided to `%s` as arguments on line %s has < 3 elements"
                                                         %
                                                         (first &form) ;&form的第一个元素是宏的名字(或者是当前命名空间的别名)
                                                         (-> &form meta :line)))))
          triples))
;(ontology [1 2 ])
;IllegalArgumentException: `[1 2]` provided to `ontology` as arguments on line 77 has < 3 elements
(ns foo)
(refer 'learnclojure.ch5.core3 :rename '{ontology triples})
;(triples [1 2])
;IllegalArgumentException: `[1 2]` provided to `triples` as arguments on line 81 has < 3 elements

(ns learnclojure.ch5.core3)
;;&form的另一个功能是保持用户提供的类型提示,大多数宏会把用户在形式上指定的元数据丢弃
;🌰
(set! *warn-on-reflection* true) ;开启反射警告
(defn first-char-of-either
  [a b]
  (get ^String (or a b) 0)) ;实际上不会这么写,而是把类型信息直接写在参数定义的时候
;;Reflection warning,.....
(first-char-of-either "aa" "bb")
;;通过打印元数据信息来看用户在or表达式上指定的类型提示信息
(binding [*print-meta* true]
  (prn '^String (or a b)))
;^{:line 95, :column 9, :tag String} (or a b),有类型信息
(binding [*print-meta* true]
  (prn (macroexpand '^String (or a b))))
;(let* [or__4469__auto__ a] (if or__4469__auto__ or__4469__auto__ (clojure.core/or b))),没有类型信息
;如果想要在or中保持用户指定的元数据信息,把&form的元数据加到宏产生的代码中
(defmacro OR
  ([] nil)
  ([x]
   (let [result (with-meta (gensym "res") (meta &form))]    ;这里用with-meta是因为特殊形式中是不能有类型提示的,所以需要先引入一个本地绑定
     `(let [~result ~x]
        ~result)))
  ([x & next]
   (let [result (with-meta (gensym "res") (meta &form))]
     `(let [or# ~x
            ~result (if or# or# (OR ~@next))]
        ~result))))
(binding [*print-meta* true]
  (prn (macroexpand '^String (OR a b))))
;(let* [or__2620__auto__ a
;      ^{:line 113, :column 22, :tag String}
;       res2635 (if or__2620__auto__ or__2620__auto__ (main.clojure.learnclojure.ch5.core3/OR b))]
;^{:line 113, :column 22, :tag String} res2635)
;;抽取出来形成函数,用于在宏中调用
(defn preserve-metadata
  [&form expr]
  (let [result (with-meta (gensym "res") (meta &form))]
    `(let [~result ~expr]
       ~result)))
(defmacro OR
  ([] nil)
  ([x] (preserve-metadata &form x))
  ([x & next]
   (preserve-metadata &form `(let [or# ~x]
                               (if or# or# (OR ~@next))))))

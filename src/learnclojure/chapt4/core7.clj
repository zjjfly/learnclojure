(ns learnclojure.chapt4.core7
  (:require [clojure.repl :as repl]))

;var
;;clojure中对一个符号求值,其实就是在当前的命名空间中寻找是这个符号的var,并对其解引用来获取其值
map
#'map
;#'是函数var的语法糖
@#'map

;;定义一个var使用def以及其衍生的一些宏,如defn,deftype,defrecord
;;def还会把符号的元数据赋到这个var上,一些特定的元数据可以改变var的语义
;;下面是例子:
;;1.私有var
;;私有var的特点是在其他命名空间中只能使用全限定名对其访问,要访问它的值只能通过解引用
(def ^:private everything 100)
(defn- go []
  (println "hehe"))
(ns learnclojure.chapt4.other-ns
  (:require [clojure.repl :as repl]))
(refer 'learnclojure.chapt4.core7)
;;使用全限定名和解引用取值
@#'learnclojure.chapt4.core7/everything
;=100
(@#'learnclojure.chapt4.core7/go)
;hehe

;;文档字符串
;clojure运行给顶层的var添加文档,文档字符串需要跟在命名符号后面
(def a
  "A simple value"
  1)
(defn b
  "A simple function"
  [c]
  (+ a c))
(repl/doc a)
;-------------------------
;learnclojure.chapt4.other-ns/a
;A simple value
(repl/doc b)
;-------------------------
;learnclojure.chapt4.other-ns/b
;([c])
;A simple function

;;文档字符串其实只是var的一个元数据
(meta #'a)
;{:line 30,
; :column 1,
; :file "/Users/zjjfly/idea works/learnclojure/src/learnclojure/chapt4/core7.clj",
; :doc "A simple value",
; :name a,
; :ns #object[clojure.lang.Namespace 0xd840d8b "learnclojure.chapt4.other-ns"]}

;;使用alter-meta!来修改文档,很少这么做,但在编写定义var的宏的时候很有用
(alter-meta! #'a assoc :doc "A dummy value!")
(repl/doc a)
;-------------------------
;learnclojure.chapt4.other-ns/a
;  A dummy value!

;;常量var
(def ^:const n 1)
;;常量会在编译的时候直接把符号替换成对应的值,这样即使之后修改了这个var的值,也不会影响之前的代码
;;如果var不是常量会引发的问题:
(def max-value 255)
(defn valid-value?
  [v]
  (<= v max-value))
(valid-value? 211)
;true
(valid-value? 256)
;false
(def max-value 500)
(valid-value? 256)
;true,修改max-value之后这个函数的行为变量,很多时候这不是我们期望的

;;使用常量就不会有上述的问题
(def ^:const max-value 255)
(defn valid-value?
  [v]
  (<= v max-value))
(valid-value? 244)
;true
(valid-value? 256)
;false
(def max-value 500)
(valid-value? 256)
;false,正确






(ns learnclojure.ch5.core1
  (:require [clojure [string :as str]
             [walk :as walk]]))

;;宏
;;在编译的时候,宏会被展开,编程编译器可以求值的clojure数据结构
;;clojure的宏的强大是因为编写宏和编写其他clojure代码所面对的接口和数据结构是一样的,而且宏中可以使用其他的宏

;使用宏实现一个for each
(defmacro foreach [[sym coll] & body]
  `(loop [coll# ~coll]
     (when-let [[~sym & xs#] (seq coll#)]
       ~@body
       (recur xs#))))
(foreach [x [1 2 3]]
         (println x))

;;使用函数无法完成这个功能,因为函数没办法把一段没有求值的代码放入循环中.所以宏可以给语言添加新的语言结构
;;自定义的宏和内置的操作符看起来没有任何区别

;;clojure已经有对应for each的宏:for和doseq
;;for类似haskell和scala的for comprehension
(for [x [1 2 3] y [4 5 6] :while (< x y)] [x y])

;;clojure和Ruby或js的eval不同的是,它是在编译期调用的,所以可以马上发现错误,而eval是运行期执行的,有错误也只会在运行的时候出现
;;还有一个区别是,eval执行的代码是字符串,而对字符串形式的代码进行操作是很脆弱也很容易出错的.所以Ruby和js都不建议使用eval

;实现一个宏让我们可以倒过来写clojure的符号
(defmacro revert-it
  [form]
  (walk/postwalk #(if (symbol? %)
                    (symbol (str/reverse (name %)))
                    %)
                 form))
(revert-it
 (qesod [gra (egnar 10)]
        (nltnirp gra)))
;;展开这个宏
(macroexpand-1 '(revert-it
                 (qesod [gra (egnar 10)]
                        (nltnirp gra))))
;(doseq [arg (range 10)] (println arg))

;;macroexpand-1只展开一次宏
;;如果想要展开知道最顶层不是宏,需要使用macroexpand,但这通常会使得展开的代码太冗长没办法看出宏的逻辑,通常还是使用macroexpand-1
(macroexpand '(revert-it
               (qesod [gra (egnar 10)]
                      (nltnirp gra))))

;;但macroexpand和macroexpand-1都不能对嵌套的宏展开,如果有这种需求,需要使用clojure.walk/macroexpand-all
(macroexpand '(cond a b c d))
;(if a b (clojure.core/cond c d))
(walk/macroexpand-all '(cond a b c d))
;(if a b (if c d nil))

;;写宏的主要的三个工具
;;1.引述(quote),用于返回参数的不求值的形式
(quote (1 2 3))
;;简写形式
'(1 2 3)

;2.语法引述,使用反引号
;和引述不同的有两点:1.无命名空间限定的符号求值成当前命名空间的符号
(def foo 123)
[foo (quote foo) 'foo `foo]
;[123 foo foo main.clojure.learnclojure.ch5.core1/foo]
;;在哪个命名空间下,语法引述的结果就是哪个命名空间下的符号
(in-ns 'bar)
`foo
;bar/foo
;;如果这个符号本身就是命名空间限定的,那么语法引述也会把这个符号求值成对应的命名空间的符号
`walk/macroexpand-all
;walk/macroexpand-all
;2.语法引述可以被反引述

;;反引述用于在宏中对一些元素进行求值,使用符号~
(in-ns 'learnclojure.ch5.core1)
`(map println [~foo])
;(clojure.core/map clojure.core/println [123])
;;反引述还可以在语法引述中对一个方法调用进行求值
`(println ~(keyword (str foo)))
;(clojure.core/println :123)

;如果有一个列表的形式,然后想要另一个列表的内容解开放入第一个列表中,这种时候使用编接反引述.符号是@
(let [defs '((def x 123)
             (def y 456))]
  `(do ~@defs))
;(do (def x 123) (def y 456))
;;这个在编写宏时很常用,比如一个接受多个形式作为代码体的宏是这样写的:
(defmacro foo-bar
  [& body]
  `(do-something ~@body))
(macroexpand-1 '(foo-bar (doseq [x (range 5)]
                           (println x))
                         :done))

;;宏是编译期被调用的,所以它不知道运行期的信息,它不能在运行期被求值,所以不能作为值传递给其他函数
(defmacro macro-hello
  [x]
  `(str "Hello," ~x "!"))
(macro-hello "jjzi")
;Hello,jjzi
;(map (macro-hello) ["jjzi"])
;clojure.lang.ArityException: Wrong number of args (0) passed to: core1/macro-hello
;;如果实在需要这样做,那么需要把宏包在一个fn或匿名函数中
(map #(macro-hello %) ["jjzi"])
;("Hello,jjzi!")
;还有一种办法是再写一个宏把map包在里面,但这样就会成为一个无底洞,所以对于需要传递高阶函数的习惯用法,宏是不适合的
;;所以可以把宏和函数一起使用,宏负责组织代码,真正的逻辑通过调用函数完成

;;宏只有在函数满足不了需求的时候才使用,主要是下面几种情况:
;;1.需要特殊的求值语义
;;2.需要特定的语法,比如设计DSL时
;;3.需要在编译器提前计算一些中间值

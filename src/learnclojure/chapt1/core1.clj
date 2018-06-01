(ns learnclojure.chapt1.core1
  (:gen-class))
;;求平均值的函数,一个简单的clojure函数的例子
(defn average
  [numbers]
  (/ (apply + numbers) (count numbers)))
(average [1,3,5,7])
;;repl中的read的原理,这两个函数把文本反序列化成对应的值和数据结构
;(read *in*)
(read-string "(+ 1 (- 2 1))")
;;repl打印的原理,它们把表达式直接打印到*out*
(pr-str [1 2 3]);打印出来并返回
(pr [1 2 3]);只是打印
;;字符串常量
"hello,world"
"hello,
zjj"
;;unicode
\u00ff
;;octal编码
\o41
;;特殊字符常量
(println (str "空格:" \space ".") )
(println (str "新行:" \newline ".") )
(println (str "换页符:" \formfeed ".") )
(println (str "回车:" \return ".") )
(println (str "退格:" \backspace ".") )
(println (str "tab:" \tab ".") )
;;关键字
;;初始化一个map
(def person {:name "zjj"
             :city "suzhou"
             ;;“::”表示是当前命名空间的关键字
             ::age 12
             })
;;访问map的city对应的值
(person :city)
(:city person)
(:learnclojure.chapt1.core1/age person)
;;name函数取得关键字的内在的名字
(name :city)
;;namespace函数求关键字的命名空间
(namespace :learnclojure.charpt1.core/age)
;; 取得包含指定关键字的一个新的map
(select-keys person [:name,:city])
;;long型数字字面量
;十六进制
(+ 0 0xff)
;八进制
(+ 0 040)
;任意进制数，r之前的数字表示几进制，最高36
(+ 0 8r125)
;;double字面量
(+ 0 3.251)
(+ 0 6.2141e12)
;;bigint
(+ 0 42N)
;;bigdecimal
(+ 0 7.63M)
;;分数
(+ 1/7 22/7)
;;正则
(class #"(p|h)int")
;;clojure的正则不需要反斜杠转义
(re-seq #"(\d+)-(\d+)" "131-686")
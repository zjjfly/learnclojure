(ns learnclojure.chapt1.core1
  (:gen-class))
;;求平均值的函数
(defn average
  [numbers]
  (/ (apply + numbers) (count numbers)))
(average [1,3,5,7])
;;字符串常量
"hello,world"
"hello,
zjj"
;;unicode
\u00ff
;;octal编码
\o41
;;特殊字符常量
\space
\newline
;;初始化一个map
(def person {:name "zjj"
             :city "suzhou"
             ;;“::”表示是当前命名空间的关键字
             ::age 12
             })
;;访问map的city对应的值
(person :city)
(:city person)
(:learnclojure.charpt1.core/age person)
;;name函数取得关键字的名字
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




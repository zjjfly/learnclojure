(ns learnclojure.chapt2.core2
  (:require [clojure.string :as str]))
;;偏函数vs函数字面量
;;函数字面量提供了偏函数功能的超集，可以通过函数字面量实现类似偏函数的功能
(#(filter string? %) [1 "D" 2 "T"])
(#(filter % [1 "D" 2 "T"]) string?)
(#(filter % [1 "D" 2 "T"]) number?)
;;函数字面量要求指定函数的所有参数，但偏函数不用
;;错误代码：(#(map *) [1 3 5] [2 4 6] [8 9 0])，没有指定参数
(#(map * % %2 %3) [1 3 5] [2 4 6] [8 9 7])
;;错误代码：(#(map * % %2 %3) [1 3 5] [8 9 7])，传入的参数和制定的参数个数不符
;;一种可行的方法是使用apply
(#(apply map * %&) [1 3 5] [2 4 6] [8 9 7])
(#(apply map * %&) [1 3 5])
((partial map *) [1 2 3] [4 5 6] [7 8 9])

;;函数的组合
;;给定一个列表的数字，返回这些数字的总和的复数的字符串形式
(defn negated-sun-str1
  [& numbers]
  (str (- (apply + numbers))))
(negated-sun-str1 1 2 4)
;;也可以写成：
;;(defn negated-sun-str1
;;       [numbers]
;;       (str (- (apply + numbers)))
;;    )
;;(negated-sun-str1 [1 2 4])

;;使用comp实现函数组合，更简洁
;;comp接受的参数个数和comp的最后一个函数接受的参数个数相等，返回值是comp第一个函数的返回值
(def negated-sun-str2
  (comp str - +))
(negated-sun-str2 3 4 7)
;;comp的作用不仅仅是写hello world这样的小玩意
;;例子：把CamelCase式的字符串转成clojure中以横线分隔的小写单词
;;interpose函数把序列的元素用指定的字符分隔,keyword函数把字符串变为关键字
(def camel->keyword1 (comp keyword
                          str/join
                          (partial interpose "-")
                          (partial map str/lower-case)
                          #(str/split % #"(?<=[a-z])(?=[A-Z])")))
(camel->keyword1 "NotBad")
;;可以使用->和->>宏实现类似的comp的功能
;;传给这些宏的第一个参数会作为后面函数的第一个(->)或最后一个参数(->>),依次类推
(defn  camel->keyword2
  [s]
  (->> (str/split  s #"(?<=[a-z])(?=[A-Z])")
       (map  str/lower-case)
       (interpose "-")
       str/join
       keyword))
(camel->keyword2 "HeHe")
;;把传入的map中的CamelCase的key转化成clojure风格的key
;;map-indexed带index的map函数
(def camel->pairs->map (comp (partial apply hash-map)
                             (partial map-indexed (fn [i x]
                                                    (if (odd? i)
                                                      x
                                                      (camel->keyword1 x))))))
(camel->pairs->map ["CamelCase" 5 "lowCamelCase" 33])
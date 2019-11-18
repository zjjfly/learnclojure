(ns learnclojure.ch11.value-equality
  (:import (java.util HashMap)))

;clojure有三种用于判断相等性的方式
;1.对象相同,即两个对象指向的是同一个实例,等价于Java的==(当比较对象时)
(identical? "foot" (str "fo" "ot"))
;false
(let [a (range 10)]
  (identical? a a))
;true
;一般来说,相同的数字从来不会是identical的,即使是字面量的比较
(identical? 5/4 (+ 1/2 3/4))
;false
;但有一个例外,Java对于一些常用的整数会做一些缓存以重用,返回时-128~127
;所以在这个范围之内的比较的结果会是true
(identical? 127 (dec 128))
;true
(identical? 128 (dec 129))
;false
;总之,不要使用identical?去比较数字

;2.引用相等,但对类型不像Java那么敏感
;clojure在比较集合的时候可能会忽视集合的具体类型,但绝不会让不同类别的集合相等(如序列绝不会等于某个set或map)
(= {:a 1 :b ["1"]}
   (into (sorted-map) [[:b ["1"]] [:a 1]])
   (doto (HashMap.)
     (.put :a 1)
     (.put :b ["1"])))
;true
;同样的,这种比较只有在两个数字的类别相同的情况下才会相同(short byte integer,long,BigInt都算是一个类别的)
(= 1 1N (Integer. 1) (Short. (short 1)) (Byte. (byte 1)))
;true
(= 1.25 (Float. 1.25))
;true
;不同类型的数字即使数值相等也不算相等(如整数绝不会相等某个整数)
(= 1 1.0)
;false
(= 1N 1M)
;false
(= 1.25 5/4)
;false

;3.数字等值,这种比较的结果和直觉理解的相符,不受我们用来划分数字表示的人为类别的影响
(== 0.125 0.125M 1/8)
;true
(== 4 4N 4.0 4.0M)
;true
;有理数和小数比较也支持
(== 1/4 0.25)
;true
;==要求参与比较的都是数字,否则会抛出异常.
;所以如果不确定是否是数字,有两种选择：
;1.使用=
;2.如果确实需要==的语义,那么要先用number?检查参数
(defn equiv?
  [& args]
  (and (every? number? args)
       (apply == args)))
(equiv? "foo" 1)
;false
(equiv? 4N 4 4.0 4.0M)
;true

;在异质类型的数字用于集合的时候,Java的数字相等的概念让人难受,因为Java的集合实现依赖与每个成员对相等的定义,所以可能会出现下面的情况
(doto (HashMap.)
  (.put (int 1) "integer")
  (.put (long 1) "long")
  (.put (BigInteger. "1") "bigint"))
;{1 "integer", 1 "long", 1 "bigint"}
;这种情况我们肯定是希望这三个键hash值一致,但Java中却不是这样的
;clojure的集合类对数字键和成员归属使用数字等值定义来确定
(into #{} [1 1N (Integer. 1) (Short. (short 1))])
;#{1}
(into {}
      [[1 :long]
       [1N :bigint]
       [(Integer. 1) :integer]])
;{1 :integer}

;对于浮点数,要特别小心
(+ 0.1 0.2)
;0.30000000000000004
;不同类型的小数的相等比较也可能出问题,及时使用的是类型不敏感的==
(== 1.1 (float 1.1))
;false
;原因是比较的时候把后面的float扩展成了double类型,而这对于1.1无法做到精确
(double (float 1.1))
;1.100000023841858
;这一行为在java中也同样会复现,根源是IEEE浮点数规范.

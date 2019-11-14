(ns learnclojure.ch11.number)

;clojure首选64位的数字类型Long,虽然所有的数学操作函数都可以接受更窄的数字类型
(class (inc (Integer. 4)))
;java.lang.Long

;clojure中的BigDecimal和BigInt的字面量写法
(class 1M)
;java.math.BigDecimal
(class 1N)
;java.math.BigInt

;clojure的数学计算方法返回的值会和传入值的类型一致
(class (dec 1M))
;java.math.BigDecimal
(class (dec 1N))
;java.math.BigInt

;clojure还支持多种数字类型的混合使用
(* 3 0.08 1/4 6N 1.2M)
;0.432

;浮点数的问题:
(+ 0.1 0.1 0.1)
;0.30000000000000004
;浮点数运算的误差的原因是和它在底层的表示方法有关系的
;clojure通过允许有理数字面量和不强制有理数为不精确的浮点数来避免这一点
(+ 1/10 1/10 1/10)
;这样的缺点是当有理数可以转为一个整数而不损失精度时,这种写法比较啰嗦
(+ 7/10 1/10 1/10 1/10)
;1N
;有理数可以转换成浮点数
(double 1/3)
;0.3333333333333333
;浮点数也可以转换成有理数
(rationalize 0.45)
;9/20

;在多种数字类型混合计算的时候,传播度最高的类型决定了计算结果的最终类型
;类型的传播度由高到低:double,BigDecimal,Rationals,BigInt,long
;这种排序保证了不会让返回值的类型是强制"有损"的.
(+ 1 1)
;2
(+ 1 1.5)
;2.5
(+ 1 1N)
;2N
(+ 1.1M 1N)
;2.2M

;double是个例外,原因是两个:
;1.double定义了一些BigDecimal不能表示的值(特别是Infinity和NaN)
;2.double是唯一的天生不准确的数字,一个涉及不准确数字的操作返回一个暗示精确的但实际不精确的类型是有问题的

;这个类型传播规则在clojure的所有的涉及到数学操作的函数都有效,因为clojure的数学操作符实际上都是clojure函数
(defn squares-sum
  [& vals]
  (reduce + (map * vals vals)))
(squares-sum 1 4 10)
;117
(squares-sum 1 4 10 20.5)
;537.5
(squares-sum 1 4 10 9N)
;198N
(squares-sum 1 4 10 9N 5.6M)
;229.36M
(squares-sum 1 4 10 25/2)
;1093/4

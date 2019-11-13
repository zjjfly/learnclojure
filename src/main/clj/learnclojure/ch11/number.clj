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



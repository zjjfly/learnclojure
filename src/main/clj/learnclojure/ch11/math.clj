(ns learnclojure.ch11.math
  (:import (java.math MathContext RoundingMode)))

;long和double提供的范围和精度通常是够用的,但在一些科学计算中可能还是不够用
;这种情况可以使用clojure.lang.BigInt和java.math.BigInteger来表示任意范围的整数,使用java.math.BigDecimal来表示任意精度的小数

;clojure自己提供一个BigInt实现的原因是因为java的BigInteger有两个问题:
;1.它的hashCode实现与Long的不一致
(.hashCode (BigInteger. "6948736584"))
;-1641197977
(.hashCode (Long. 6948736584))
;-1641198007
;这个问题在Java中可能问题不大,但在clojure这种动态语言中很糟糕,会导致同一个数字因为类型不同可能在集合中出现两次
;2.BigInteger的所有操作必须使用基于软件的实现,clojure会在可能使用原始类型的时候使用原始类型,提高了性能
;总之,clojure的所有数字操作的语义对于不同的数字类型都是一致的,而且返回的结果不会是BigInteger类型的.

;有时候需要使用比long或double能表示的值还要大的值,如果仍使用Long或Double则会抛出异常.
;clojure不会让整数溢出的时候默默的环绕值
(def k Long/MAX_VALUE)
k
;9223372036854775807
(try
  (inc k)
  (catch Exception e
    (.printStackTrace e)))
;java.lang.ArithmeticException: integer overflow
;如果确实需要这么大的值,有几个选择:
;一.明确使用任意精度的值
(inc (bigint k))
;9223372036854775808N
(* 100 (bigdec Double/MAX_VALUE))
;1.797693134862315700E+310M
;或者使用BigInt和BigDecimal的字面量
(dec 1022337263654715900N)
;1022337263654715899N
(* 0.5M 1e403M)
;5E+402M
;整数字面量在long的范围的时候会自动提升为BigInt
11223344556677889900
;11223344556677889900N

;二.为整数计算使用自动提升的操作符
;如果可以控制对特定函数的输入,使用任意精度的类型作为参数,并使用clojure提供的算数操作符,这样数字类型的传播规则会保证返回值类型不会有问题
;如果在一种计算或算法中只是有可能超过long的范围,而类型传播不足以保证结果正确,那么可以使用clojure里带撇号的自动提升的算数操作符来自动提升Long类型的结果为BigInt
;这些操作符包括:inc' dec' +' -' *',使用它们会有一定的性能的开销
(inc' k)
;9223372036854775808N
;这些操作符只在必要的时候提升
(inc' 1)
;2
(inc' (dec' k))
;9223372036854775807

;如果有的时候你需要保留Java的那种数据溢出的时候环绕值这一行为,可以使用无检查的操作符,它们以unchecked-开头
(unchecked-dec Long/MIN_VALUE)
;9223372036854775807
(unchecked-multiply 92233720368547758 1000)
;-80
;这些变体比较冗长,但可以使用set!把全局变量*unchecked-math*设置为true,这就让所有的clojure的算数操作符都不会进行溢出检查
(set! *unchecked-math* true)
(+ Long/MAX_VALUE 1)
;-9223372036854775808
;*unchecked-math*只能在命名空间中修改,所以一般会在需要关闭溢出检查的时候把它设置为true,然后在不需要的时候设置回false
(set! *unchecked-math* false)
;一般不用binding来设置*unchecked-math*,因为binding只在求值的时候生效,而*unchecked-math*是控制编译器行为的
;所以这个修改可能在很久之后才会生效,这显然不符合预期


;BigDecimal的一些操作会抛出异常
(try
  (/ 1M 3M)
  (catch Exception e
    (.printStackTrace e)))
;java.lang.ArithmeticException: Non-terminating decimal expansion
;为了不报错,需要加上指定取整模式和最大刻度的参数
(.divide 1M 3M 2 BigDecimal/ROUND_HALF_UP)
;这个代码的缺点是比较冗长,所以clojure提供了一个宏,只要提供给它想要的取整模式和刻度,它们会被传递到这个宏范围内的所有BigDecimal操作
(with-precision 10 (/ 22M 7))
;3.142857143M
(with-precision 10 :rounding FLOOR
  (/ 22M 7))
;3.142857142M
;还有一种方式是直接修改动态var*math-context*
(set! *math-context* (MathContext. 10 RoundingMode/FLOOR))
(/ 22M 7)
;3.142857142M

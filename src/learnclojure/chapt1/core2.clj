(ns learnclojure.chapt1.core2)
;;clojure的注释有两种，一种是以分号开头的单行注释
;;另一种是宏#_，会忽略下一个clojure形式,可以用于注释掉开发的时候写入的打印语句
(read-string "(+ 1 2 #_(* 2 2) 9)")
;;定义一个var，它不是变量
(def x 1)
x
(def x "hello")
(inc 1)
x
;;*ns* 当前命名空间
*ns*
;;创建一个叫foo的命名空间,并把当前命名空间切换到新建的这个命名空间
(ns foo)
*ns*
;;通过命名空间访问var
learnclojure.chapt1.core2/x
;;foo中没有x，所以报错
;x
;;java.lang包下的类被默认引入每个clojure命名空间
String
Integer
;;没有引入的java类要用包名去访问
java.util.ArrayList
;;clojure.core中所有的var也是默认引入的
filter
;;阻止求值,用特殊形式quote或者',对于任何clojure形式都可以使用
'x
(quote x)
(symbol? 'x)
;;对列表使用
(list? '(+ x x))
;;list 函数可以把后面所有参数组装列表
(list '+ 'x 'x)
;;quote的一个应用是探查reader对于任意一个clojure形式的求值结果
''x
'@x ;@相当于函数deref,解引用,具体用法见http://clojure-api-cn.readthedocs.io/en/latest/clojure.core/deref.html
'#(+ % %)
`(1 (dec 3) 3) ;(1 (clojure.core/dec 3) 3)
`(1 ~(dec 3) 3) ;(1 2 3) 符号~相当于函数unquote,它的作用是解析出该符号在当前命名空间中的值,一般用于syntax-quote中
'`(a b ~c) ;反引号`的语法叫做syntax-quote,和'类似,会把它修饰的list中的每一项都解析为带命名空间的符号
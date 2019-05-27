(ns learnclojure.ch8.core1)

;当前命名空间
*ns*
(defn a [] 42)
;#object[clojure.lang.Namespace 0x27b9c81 "learnclojure.ch8.core1"]
;转到其他命名空间
(in-ns 'charpt1.core1)
(def ^:const planck 6.21e-1)
;#'learnclojure.ch1.core1/planck

;下面的代码会报错,因为in-ns不像ns那样会默认(refer 'clojure/core)
;(+ 1 1)

;使用refer可以把某个命名空间中的共有var加入当前命名空间
(clojure.core/refer 'learnclojure.ch8.core1)
(a)

;refer可以通过关键字参数:only,:exclude,:rename来指定,排除和重命名导入的var
(clojure.core/refer 'clojure.core
                    :exclude '(range)
                    :rename '{+ add
                              - sub
                              / div
                              * mul})
(-> 5 (add 18) (mul 2) (sub 6))
(refer 'clojure.string
       :only '(blank?))
(blank? "")

;refer很少直接用,一般都是通过use间接的使用它
;use和require都可以确保目标命名空间被载入,并为其指定别名,但use还可以让代码不需要写限定就可以引用其他空间的var
(require 'clojure.set),
;require只导入lib,但不会绑定到当前命名空间,所以使用起来还是需要全限定名
(clojure.set/union #{1 2 3} #{3 4 5})
;如果你嫌全限定名太长,可以给它起个别名
(require '[clojure.set :as set])
(set/union #{1 2 3} #{3 4 5})
;require也可以一次性导入多个前缀相同的命名空间,通过提供一个顺序性集合类,第一个元素是命名空间的前缀,其余的是命名空间的剩余字段
(require '(clojure string [set :as set]))
(set/union #{1 2 3} #{3 4 5})

(use 'clojure.xml)
;等价于
;(require 'clojure.xml)
;(refer 'clojure.xml)

;使用use实现复杂的导入需求
(use '(clojure [string :only (join) :as str]
               [set :exclude (join)]))
(join "," [1 2 3])

;import一般用于导入Java类,效果和Java的import一样
(import 'java.util.Date 'java.text.SimpleDateFormat)
(.format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (Date.))
;可以一次import多个类,语法和require类似
(import '(java.util Arrays Collections))
(->> (iterate inc 0)
     (take 5)
     into-array
     Arrays/asList
     Collections/max)
;4

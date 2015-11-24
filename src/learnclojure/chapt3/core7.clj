(ns learnclojure.chapt3.core7)
;;clojure提供了一些具体的数据结构供我们使用，它们都实习了一个或多个抽象
;;它们的区别大多在集合的构建方式

;;列表 list
;;一般都是用来做函数调用的，用来存储数据的场景不多，这和其他lisp方言不同，因为clojure使用更丰富的数据解构，如map，set，vector和序列。
;;序列在很大程度上跟list是相似的，这使得我们几乎在代码里不会直接用到list

;clojure的list是单向链表，所以只对链头支持高效的访问和修改(conj，pop，rest等函数)，由于是链表，所以不支持高效的随机访问。
;;它也不支持get，因为它的性能达不到get亚线性的要求。

;;列表是自身的序列，所以调用seq始终返回列表本身
;;真正用来存放数据的列表是这么用的
'(1 2 3)
;=(1 2 3)
;;没有引号会把1看成函数，1肯定不是函数，所以会报错
;;这个引号的副作用是，这个列表里的所有元素都不会被求值
'(1 2 (+ 1 2))
;;这种场景下，一般都用vector，如果真的需要用list(一般在写宏的时候)，可以用list函数
(list 1 2 (+ 1 2))
;;list会把每一个参数作为列表的一个元素
;;用list？检查是否一个集合是否是列表
(list? '(2 1))

;;vector
;;vector是一种顺序数据结构，支持高效的随机访问和更改语义,类似java的arraylist
;;它实现了associative，indexwd，stack
;;除了熟悉的字面量，还可以通过vector和vec函数来创建：
(vector 1 2 3)
(vec (range 10))
;;vector和list的作用类似，而vec只接收一个参数，把传入的集合转换成一个新的vector。

;;谓词vector?也和list？类似，测试一个值是否是vector。
(vector? [1 2 3])

;;元组(tuple)是vector最常见的使用场景,任何你想把多个值绑在一起处理，比如从一个函数返回多个值，就可以把多个值放在vector里
(defn euclidia-devision-1
  "docstring"
  [x y]
  [(quot x y) (rem x y)])
(euclidia-devision-1 42 8)
;;还有一个更简洁的写法 ((juxt a b c) x) = [(a x) (b x) (c x)]
(defn euclidia-devision-2
  "docstring"
  [x y]
  ((juxt quot rem) x y))
(euclidia-devision-2 42 8)

;;这个可以和解构很好的配合
(let [[q r] (euclidia-devision-2 68 9)]
  (str "68/9=" q "*9+" r))

;;虽然vector和tuple一样易用，但记住，它终究是不适合暴露到系统外部的数据结构。对它的使用最好保持在类库或组件的内部，而不要在公共api这么用。
;;原因有两个：1、tuple是不能自我注解的，看到一个tuple你不知道每个值的含义时什么，必须回忆或回到tuple赋值的地方才行。
;;2、tuple是不灵活的。要构建一个tuple，需要提供tuple所有的字段，即使tuple某个字段对于你的场景没有意义，还是要提供，而且只能向tuple尾部添加元素。
;;map不受这种限制，所以你要用在公共API的参数或返回值上，map更合适。
;;但在某些情况下，tuple的字段的含义是非常明确的，那用vector也是没有问题的，比如坐标，几何图形的边等。
(def point-3d [24 64 -14])
(def travel-legs [["LYS" "FRA"] ["FRA" "PHL"] ["PHL" "RDU"]])

;;set
;;作为一个具体类型的数据结构没什么说的，前面已经讨论过。
;;下面的式子会报错，因为set中不能有重复的
;#{1 2 3 3}
;;hash-set能创建任意数量元素的无序set
(hash-set 1 2 4 5 )
;;可以用set函数把其他类型的集合转成set
(set [1 3 4 5 1])
;= #{1 4 3 5}
;;这个函数可以用于任何可序列化的值，而且因为set本身也是函数，所以可以利用它写出很简洁的代码
;;删除字符串中的元音字母
(apply str (remove (set "aeiouy") "vowels are  useless"))
;;判断是否是数字的谓词
(defn numeric? [s] (every? (set "0123456789") s))
(numeric? "11414")
;=true
(numeric? "11dad414")
;=false
;;也可以定义有序的set，前面已经介绍过了。

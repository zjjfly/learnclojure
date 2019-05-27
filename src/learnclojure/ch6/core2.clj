(ns learnclojure.ch6.core2)

;clojure中的类型
;定义类型的两种方法
;类名和协议一样采用驼峰命名法,因为它们会被编译成Java类
(deftype Point [x y])
(defrecord Circle [x y r])
;创建类型的实例,和调用Java代码一样
(Point. 1 2)
(Circle. 1 2 3)
;访问字段,也和调用Java代码一样
(.x (Point. 1 2))

;字段的类型都是Object,可以使用类型提示指定类型,但它不会改变实际的类型
(defrecord NamedPoint [^String name ^long x ^long y])

;有时候知道一个类型有哪些字段很有用,特别是记录类型,它可以动态的添加字段
(NamedPoint/getBasis)
;[name x y]
;这个basis保留了定义时的所有信息,包括元数据
(map meta (NamedPoint/getBasis))
;({:tag String} {:tag long} {:tag long})

;记录类型(record),是用来表示应用级别的数据的,可以认为它和map是clojure的POJO
;类型(type)是用来表示一些底层的类型的,比如数据结构
;defrecord对于所定义的类型提供了Clojure和Java的互操作的一些默认行为
;deftype提供了一些底层操作进行优化的能力
;大多数时候使用defrecord,很少使用deftype

;clojure还提供了比较弱的struct map实现,可以通过defstruct,struct-map,create-struct定义,但不推荐使用这些
;如果想要一个灵活的struct,直接使用map,如果需要一个比较正式的模型,使用记录类型.

;;定义的类型会被放入所在命名空间对应的一个java包中,所以在当前命名空间可以直接使用,但在其他命名空间中,需要import这个类才能使用,仅仅use或require这个命名空间还是不行
(ns core21)
(refer 'learnclojure.ch6.core2)
;(Circle. 1 1 2)
;IllegalArgumentException: Unable to resolve classname: Circle
(import 'learnclojure.ch6.core2.Circle)
(Circle. 1 1 2)
;#learnclojure.ch6.core2.Circle{:x 1, :y 1, :r 2}
(in-ns 'learnclojure.ch6.core2)
;defrecord其实是deftype的变体,它添加了下面的额外特性:
;1.值语义
;值语义意味着记录类型是不可变的,而且如果两个记录的所有字段都相等,那么这两个记录就认为是相等的,它是通过自动实现equals和hashCode方法实现的
(= (Circle. 1 2 3) (Circle. 1 2 3))
;true
(= 3 3N) ;数字后加N表示这个数字是bigint的字面量
;true
(= (Circle. 1 2 3) (Circle. 1N 2N 3N))
;true

;2.实现了关系型数据结构语义
;所有对map的操作都可以用在记录类型上,因为它实现了Associative接口
(:x (Circle. 1 2 3))
(assoc (Circle. 1 2 3) :y 1)
;可以添加新的字段
(let [p (assoc (Circle. 1 2 3) :z 5)]
  (dissoc p :x))
;这里额外添加的字段实际没有添加到底层的Java类中,而是放到了一个单独的clojure的hashmap中
;(.z (assoc (Circle. 1 2 3) :z 5))
;IllegalArgumentException: No matching field found: z for class learnclojure.ch6.core2.Circle

;记录类型还实现了java.util.Map接口,所以可以把记录类型传给任意接受Map作为参数的方法

;3.元数据的支持
;和其他集合一样,支持元数据,而且不影响值语义,也就是元数据不参与对象相等性的比较
(-> (Circle. 1 2 3)
    (with-meta {:foo :bar})
    meta)

;4.对clojure reader的支持,可以直接通过clojure reader读入一个记录类型
;对于记录类型,repl使用一种特殊的表示法来打印出来
(Circle. 1 2 3)
;#learnclojure.ch6.core2.Circle{:x 1, :y 2, :r 3},这是一个记录字面量,和vector的[],关键字的冒号是一样的
;这意味着reader读入这个字符串可以直接解析为一个记录类型
(= (read-string "#learnclojure.ch6.core2.Circle{:x 1, :y 2, :r 3}")
   (Circle. 1 2 3))
;true
;这意味使用记录类型来存储和获取数据是很方便的,和json一样

;5.一个额外的方便的构造函数,使得我们可以在构造实例的时候添加一些元数据和一些额外的字段
;它提供了额外的构造函数多了两个参数,一个是元数据的map,一个是额外字段的map
(def circle (Circle. 1 2 3 {:foo :bar} {:z 4}))
;#learnclojure.ch6.core2.Circle{:x 1, :y 2, :r 3, :z 4}
(meta circle)
;{:foo :bar}

;构造函数不应该作为公开的API的一部分,而是应该提供工厂函数,因为
;1.工厂函数更适合调用者使用,因为deftype和defrecord生成的构造函数太底层,包含了一些调用者不关心的细节在里面
;2.可以把工厂函数作为普通函数一样传给其他高阶函数
;3.可以最大化你的API的稳定性,即使在底层模型发生变化的时候

;如果类型的字段增加了,可以在工厂函数中增加一些逻辑,为这个字段设置默认值,这样就一定程度保证了这个API的稳定性
;clojure不支持自定义构造函数,所以你想写在构造函数里的逻辑都应该写到工厂函数

;defrecord和deftype都会自动生成一个形如->MyType的工厂函数
(->Point 3 1)
(->Circle 1 2 1)
;记录类型还会生成一个叫map->MyType的工厂方法,接受一个map作为参数
(map->Circle {:x 1 :y 2 :z 4 :r 3})
;这两个工厂函数和高阶函数结合会很有用
(apply ->Point [1 2])
(map (partial apply ->Point) [[1 2] [3 4]])
(map map->Circle [{:x 1 :y 2 :r 3}
                  {:x 4 :y 5 :r 6}
                  {:x 3 :y 2 :r 1}])
;对于记录类型,map->MyType还可以通过静态方法create访问,这对于Java调用者很方便
(Circle/create {:x 3 :y 2 :r 1})
;虽然这些工厂函数很有用,但很多时候还是需要自己定义工厂方法,比如对一些字段做一些校验
(defn get-circle
  [x y r]
  {:pre [(pos? r)]} ;:pre用于函数开头的校验,:post用于对返回值校验
  (->Circle x y r))
;(get-circle 1 2 -1)
;AssertionError: Assert failed: (pos? r)

;一般工厂函数用于把普通map转换成记录类型的时候,当然也可以相反
(defn map-circle
  [{:keys [x y r]}]
  {:pre [(pos? r)]}
  (->Circle x y r))
(map-circle {:x 3 :y 2 :r 1})
;#learnclojure.ch6.core2.Circle{:x 3, :y 2, :r 1}
(defn circle-map
  [{:keys [x y r]}]
  {:x x :y y :r r})
(circle-map (get-circle 1 2 3))
;{:x 1, :y 2, :r 3}

;如果想把一个原来使用普通map作为模型的代码改成使用记录类型,只要提供一个从map转成记录类型的工厂方法就可以,其他操作map的的代码完全不需要修改
;什么时候使用map,什么时候使用记录类型?
;1.优先使用map
;2.一般是需要基于类型的多态的时候,或者需要提高对字段的访问性能的时候,才考虑使用记录类型

;从map转到记录类型有一个陷阱:记录类型自身不是函数
;((get-circle 1 2 3) :x)
;ClassCastException: learnclojure.ch6.core2.Circle cannot be cast to clojure.lang.IFn

;还有一个陷阱是:记录类型和map永远不可能相对
(def circle (get-circle 1 2 3))
(= circle (circle-map circle))
;false
(= (circle-map circle) circle)
;false

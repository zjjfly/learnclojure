(ns learnclojure.ch6.core3
  (:import (clojure.lang IDeref)
           (learnclojure.ch6.core1 Matrix)
           (java.util List)
           (java.awt.event ActionListener)
           (java.io File FileFilter)))

;deftype用于定义最底层的框架类型,比如新的数据结构或引用类型
;而普通map以及记录类型用于表示应用级别的类型

;它提供了在编写底层的应用或库的时候不可避免的特性:可修改的字段
;但普通的字段访问还是只能通过Java互操作的语法
(deftype Point [x y])
(.x (Point. 1 2))
;1
;deftype定义的类型没有实现associative接口,所以无法使用关键字来访问字段
(:x (Point. 1 2))
;nil

;deftype定义的类型最终被编译成Java类,并且其中的不可变字段以public final修饰
;可修改字段需要通过元数据声明:^:volatile-mutable和^:unsynchronized-mutable
;volatile-mutable和Java中的volatile作用一致,可以保证可见性和顺序性,unsynchronized-mutable修饰的字段是普通的Java字段,则无法保证线程安全
(deftype MyType [^:volatile-mutable fld])
;;可修改字段是private的,且只能在定义这个类型的形式中的那些内联方法中使用
(deftype SchrodingerCat [^:unsynchronized-mutable state]
  IDeref
  (deref [sc]
    (locking sc
      (or state
          (set! state (if (zero? (rand-int 2))
                        :dead
                        :alive))))))
(defn schrodinger-cat
  []
  (SchrodingerCat. nil))
(def felix (schrodinger-cat))
@felix
;:alive
(schrodinger-cat)
;#object[main.clj.learnclojure.ch6.core3.SchrodingerCat 0x46a8edd0 {:status :ready, :val :dead}]

;一般情况下应用的可修改性需求可以通过clojure的引用类型,数据流类型(future,promise,delay)来满足,或者使用java.util.concurrent里的类型

;内联实现协议,在deftype或defrecord的时候直接实现协议的方法
(defrecord Point2D [x y]
  Matrix
  (lookup [_ i j]
    (when (zero? j)
      (case i
        0 x
        1 y)))
  (update! [pt i j value]
    (if (zero? j)
      (condp = i
        0 (Point. value y)
        1 (Point. x value))
      pt))
  (rows [_] [[x] [y]])
  (cols [_] [[x y]])
  (dims [_] [2 1]))
;内联实现和extend-*的区别是,extend-*访问类型的字段需要使用关键字或者Java互操作方法,而内联实现可以直接使用字段名访问

;内联实现一般性能比较好, 原因有二:1.内联实现可以直接访问类型的字段;2.内联实现的时候调用协议的方法跟Java中调用接口的方法一样快
;因为每个协议会被编译成一个接口,内联实现会生成这个接口的实现类

;内联实现的缺点:如果有两个协议有相同的方法,就没法以内联的方式为这两个冲突的方法提供实现;如果你想要实现defrecord会自动提供的方法,也会报错
(defprotocol ClashWhenInlined
  (size [x]))
;(defrecord R []
;  ClashWhenInlined
;  (size [x]))
;ClassFormatError: Duplicate method name&signature in class file ch6/core3/R
;使用extend-type就不会有这个问题,因为它只是把一个类型的实现注册到协议,不会改变类R本身

;内联实现的实现代码是直接写入类文件中的,所以无法在运行的时候修改实现,如果真的修改了实现,那么所有依赖这个类的代码都需要重新编译
;最致命的问题是,deftype和defrecord都会创建一个新的类型,因此在它之前已经存在的对象无法使用任何更新的内联实现
;由于内联实现的这种静态属性和clojure的理念不合,所以一般不使用内联实现,只有在需要优化性能或实现Java接口的时候才使用

;实现Java接口
(deftype MyType [a b c]
  Runnable
  (run [_]
    (println "Running..."))
  Object
  (toString [_]
    "MyType")
  List
  (size [_]
    1))
;实现了List的size方法,如果调用这个接口中的其他方法会报错
;(.isEmpty (MyType. 1 2 3))
;java.lang.AbstractMethodError: Method ch6/core3/MyType.isEmpty()Z is abstract,

;通过对Object的方法重写让deftype定义的类型也有值语义
(deftype Point2D [x y]
  Matrix
  (lookup [_ i j]
    (when (zero? j)
      (case i
        0 x
        1 y)))
  (update! [pt i j value]
    (if (zero? j)
      (condp = i
        0 (Point. value y)
        1 (Point. x value))
      pt))
  (rows [_] [[x] [y]])
  (cols [_] [[x y]])
  (dims [_] [2 1])
  Object
  (equals [_ that]
    (and (instance? Point2D that)
         (= x (.x that)) (= y (.y that))))
  (hashCode [_]
    (-> x hash (hash-combine y))))
(= (Point2D. 1 2) (Point2D. 1 2))
;true

;除了deftype和defrecord还有一种内联实现:reify,它相当于Java中的匿名内部类
;它也不需要对所有方法都实现
(reify
  Runnable
  (run [_]
    (println "Running")))
;使用reify会创建一个闭包,它可以访问当前词法范围的所有本地绑定,所以常用创建适配器或一次性对象
;把一个函数包装成一个listener
(defn listener
  [f]
  (reify
    ActionListener
    (actionPerformed [_ e]
      (f e))))
;实现FileFilter来过滤文件
(doseq [file (.listFiles (File. ".")
                         (reify
                           FileFilter
                           (accept [_ f]
                             (.isDirectory f))))]
  (println file))
;reify和deftype和defrecord一样是直接把实现嵌入到类中,所以不能动态更新,但调用起来不会有额外开销.它只能实现协议,接口和Object类,不能继承具体的类即使是抽象类

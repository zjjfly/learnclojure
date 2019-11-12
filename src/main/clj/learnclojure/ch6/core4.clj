(ns learnclojure.ch6.core4)

;extend,可以达到类似继承效果
;把对一组方法的实现放到一个map中,用extend把这组实现绑定到类型,并且可以修改这个map中的实现,这样就实现了类似继承,mixin,trait等效果
(defprotocol Matrix
  "protocol for working with 2D data structures"
  (lookup [matrix i j])
  (update! [matrix i j value])
  (rows [matrix])
  (cols [matrix])
  (dims [matrix]))
(defrecord Point [x y])
(extend Point
  Matrix
  {:lookup  (fn [pt i j]
              (when (zero? j)
                (case i
                  0 (:x pt)
                  1 (:y pt))))
   :update! (fn [pt i j value]
              (if (zero? j)
                (condp = i
                  0 (Point. value (:y pt))
                  1 (Point. (:x pt) value))
                pt))
   :rows    (fn [pt]
              [[(:x pt)] [(:y pt)]])
   :cols    (fn [pt]
              [[(:x pt) (:y pt)]])
   :dims    (fn [_]
              [2 1])})
(extenders Matrix)
(rows (Point. 1 2))
;使用extend可以实现类似模板模式的效果,先定义一个map放一些和通用方法的实现,在使用extend的时候在那个map中添加一些和具体的类相关的方法的实现
(def abstract-matrix-impl
  {:cols (fn [pt]
           (let [[h w] (dims pt)]
             (map (fn [x]
                    (map #(lookup pt x %) (range 0 w))
                    (range 0 h)))))
   :rows (fn [pt]
           (apply map vector (cols pt)))})
(defrecord Point2D [x y])
(extend Point2D
  Matrix
  (assoc abstract-matrix-impl
         :lookup (fn [pt i j]
                   (when (zero? j)
                     (case i
                       0 (:x pt)
                       1 (:y pt))))
         :update! (fn [pt i j value]
                    (if (zero? j)
                      (condp = i
                        0 (Point2D. value (:y pt))
                        1 (Point2D. (:x pt) value))
                      pt))
         :dims (fn [_]
                 [2 1])))
(lookup (->Point2D 1 2) 1 0)

;一个使用extend实现mixin的例子
(defprotocol Measurable
  (width [measurable] "Return the width in px")
  (height [measurable] "Return the height in px"))
(defrecord Button [text])
(extend-type Button
  Measurable
  (width [btn]
    (* 8 (-> btn :text count)))
  (height [_] 8))
(def bordered
  {:width  #(* 2 (:bordered-width %))
   :height #(* 2 (:bordered-height %))})
;protocol中有很多字段,其中有一个字段impls,它是一个存放了协议的所有实现的map
(get-in Measurable [:impls Button])
(defn combine
  [op f g]
  (fn [& args]
    (op (apply f args) (apply g args))))
(defrecord BorderedButton [text bordered-width bordered-height])
(extend BorderedButton
  Measurable
  (merge-with (partial combine +)
              (get-in Measurable [:impls Button])
              bordered))
(let [btn (Button. "Hello World")]
  [(width btn) (height btn)])
(let [bbtn (BorderedButton. "Hello World" 6 4)]
  [(width bbtn) (height bbtn)])
;不要过早的对协议过早的内联实现,因为上面这样的方法不支持内联实现

;获取实现协议的所有类
(extenders Measurable)

;判断某个类是否实现了协议
(extends? Measurable Button)
;true

;判断某个对象是否实现了某个协议,类似Java的instanceof
(satisfies? Measurable (Button. "test"))
;true
;内联实现的实例也可以用satisfies?来判断,效果和instance?一样
(deftype Foo [x y]
  Measurable
  (width [_] x)
  (height [_] y))
(satisfies? Measurable (Foo. 1 2))
;true
(instance? learnclojure.ch6.core4.Measurable (Foo. 1 2))
;true

;不要把一个协议扩展到类继承链上的两个类
(defprotocol P
  (a [x]))
(extend-protocol P
  java.util.Collection
  (a [x] :collection!)
  java.util.List
  (a [x] :list!))
;如果这么做,协议的方法的分派机制会使用类的继承层次来决定,它始终使用层级比较低的类的实现
(a [])
;:list!

;如果扩展协议的两种类型没有关联,但一个类同时实现了这两种类型,那么分派机制会随机分派
(extend-protocol P
  java.util.Map
  (a [x] :map!)
  java.io.Serializable
  (a [x] :serializable!))
(a {})

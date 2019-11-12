(ns learnclojure.ch9.clj-operate-java
  (:import (java.util ArrayList LinkedHashMap)
           (java.awt Point)
           (java.io File)
           (java.net URL))
  (:require [clojure.java.io :as io]))

;调用java类构造方法
(slurp (URL. "https://cnn.com"))

;访问java类静态成员
Double/MAX_VALUE
;调用java类静态方法
(Double/parseDouble "1.2")
;互操作和一般的clojure函数调用一样,函数开头后面跟着参数,所以可以使用->,->>这样的便利操作
(defn decimal-to-hex
  [^String x]
  (-> x
      Integer/parseInt
      (Integer/toString 16)
      .toUpperCase))
(decimal-to-hex "255")

;修改对象的成员
(def ^Point pt (Point. 5 10))
(.x pt)
(set! (.x pt) -4)
(println pt)

;一些便利的互操作工具
(instance? Long 1)
(class 1)
;java有很多对象在new之后做一些额外的初始化工作,这个时候可以使用doto,它特别适合对单个对象连续进行多个操作
;doto最后返回的仍然是第一个参数
(doto (ArrayList.)
  (.add 1)
  (.add 2)
  (.add 3))

;异常处理
(defn as-int
  [^String s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e
      (.printStackTrace e))
    (finally (println "Attempt to parse as integer: " s))))
(as-int "1")
;clojure不需要捕获检查型异常
(File/createTempFile "tempClj" "txt")

;使用with-open实现类似java7的try-with-resources语法
(defn append-to
  [^File f ^String text]
  (with-open [w (io/writer f :append true)]
    (doto w (.write text) .flush)))
(append-to (File. "1.txt") "he")

;类型提示
(defn length-of
  [^String text]
  (.length text))
(length-of "sss")
;类型提示在进行互操作的时候用,为了避免编译器使用反射性的互操作,其他时候一般不需要
;把*warn-on-reflection*设置为true,可以让编译器在使用反射性操作的时候产生警告,这有助于我们知道潜在的性能问题
(set! *warn-on-reflection* true)
(defn capitalize
  [s]
  (-> s
      (.charAt 0)
      Character/toUpperCase
      (str (.substring s 1))))
;repl会有三个反射性操作警告
(capitalize "zjj")
;测试一下性能
(time (doseq [s (repeat 100000 "foo")]
        (capitalize s)))
;9xx ms
;加上类型标签再次测试一些性能
(defn capitalize
  [^String s]
  (-> s
      (.charAt 0)
      Character/toUpperCase
      (str (.substring s 1))))
(time (doseq [s (repeat 100000 "foo")]
        (capitalize s)))
;3x ms

;任何表达式都可以加类型提示
(defn split-name
  [user]
  (.split ^String (:name user) ""))
(split-name {:name "Zi Junjie"})
;还可以对函数返回值进行提示
(defn file-extension
  ^String [^File f]
  (->> f
       .getName
       (re-seq #"\.(.+)")
       first
       second))
(file-extension (File. "1.txt"))

;clojure的数组操作是少数的比Java繁琐的地方
;集合转成数组
(into-array [1 2 3])
;初始化数组
(make-array Integer 10 100)
;初始化原始类型数组
(long-array 10)
(make-array Long/TYPE 10)
;给原始类型加类型提示比较麻烦
(def ^{:tag (Class/forName "[I")} array (make-array Integer/TYPE 10))
;赋值
(aset array 0 1)
;取值
(aget array 0)
;在gist上找到的一个自动获取原始类型数组的方法
(defn array-type
  "Return a string representing the type of an array with dims
  dimentions and an element of type klass.
  For primitives, use a klass like Integer/TYPE
  Useful for type hints of the form: ^#=(array-type String) my-str-array"
  ([klass] (array-type klass 1))
  ([klass dims]
   (.getName (class
              (apply make-array
                     (if (symbol? klass) (eval klass) klass)
                     (repeat dims 0))))))
;修改这个类,让它直接返回Class
(defn array-type
  "Return a string representing the type of an array with dims
  dimentions and an element of type klass.
  For primitives, use a klass like Integer/TYPE
  Useful for type hints of the form: ^#=(array-type String) my-str-array"
  (^Class [klass] (array-type klass 1))
  (^Class [klass dims]
   (class
    (apply make-array
           (if (symbol? klass) (eval klass) klass)
           (repeat dims 0)))))
;利用这个新方法给原始类型数组标记
(def ^{:tag (array-type Integer/TYPE 1)} array (make-array Integer/TYPE 10))

;使用proxy产生一个匿名类的实例,类似reify,但proxy最好只在从一个具体的类派生子类的时候使用
;使用proxy实现一个LRU缓存
(defn lru-cache
  [max-size]
  (proxy [LinkedHashMap] [16 0.75 true]
    (removeEldestEntry [_]
      (> (count this) max-size))))
(def ^LinkedHashMap cache (doto (^LinkedHashMap lru-cache 5)
                            (.put :a :b)))
(doseq [[k v] (partition 2 (range 500))]
  (get cache :a) ;访问:a,这样他就不会被移除
  (.put cache k v))
cache

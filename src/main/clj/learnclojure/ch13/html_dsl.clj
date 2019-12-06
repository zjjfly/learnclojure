(ns learnclojure.ch13.html-dsl
  (:use [clojure.test])
  (:require [clojure.string :as string]))

;写一个简单的类似hiccup的html生成器
;它可以把下面这段代码
;[:html
;  [:head [:title "Propaganda"]]
;  [:body [:p "Visit us at "
;           [:a {:href "http://clojureprogramming.com" }
;            "our website"]
;           "."]]]
;转成下面的html
;<html>
;<head>
;    <title>Propaganda</title>
;</head>
;<body>
;<p>Visit us at <a href="http://clojureprogramming.com">our website</a>.</p>
;</body>
;</html>

;使用are可以避免写过多的(is (= expected (f input)))这样的表达式
(deftest test-addtion
  (are [x y z] (= x (+ y z))
    10 7 3
    20 10 10
    100 89 11))
(run-tests)
;are有助于减少每个断言的重复,但仍然有需要重复这个转换
;可以写一个宏来避免
(defmacro are* [f & body]
  `(are [x# y#] (~'= (~f x#) y#)
     ~@body))
(deftest test-tostring
  (are* str
        10 "10"
        :foo ":foo"
        "identity" "identity"))
(run-tests)

;先写几个测试,使用TDD开发模式
(declare html attrs)

(deftest test-html
  (are* html
        [:html]
        "<html></html>"

        [:a [:b]]
        "<a><b></b></a>"

        [:a {:href "/"} "Home"]
        "<a href=\"/\">Home</a>"

        [:div "foo" [:span "bar"] "baz"]
        "<div>foo<span>bar</span>baz</div>"))
(deftest test-attrs
  (are* (comp string/trim attrs)
        nil ""

        {:foo "bar"}
        "foo=\"bar\""

        (sorted-map :a "b" :c "d")
        "a=\"b\" c=\"d\""))

(defn attrs
  [attr-map]
  (->> attr-map
       (mapcat (fn [[k v]] [\space (name k) "=\"" v "\""]))
       (apply str)))
(defn html
  [x]
  (if-not (sequential? x)
    (str x)
    (let [[tag & body] x
          [attr-map body] (if (map? (first body))
                            [(first body) (rest body)]
                            [nil body])]
      (str "<" (name tag) (attrs attr-map) ">"
           (apply str (map html body))
           "</" (name tag) ">"))))

(html [:html
       [:head [:title "Propaganda"]]
       [:body [:p "Visit us at "
               [:a {:href "http:// clojureprogramming.com"}
                "our website"]
               "."]]])
;<html><head><title>Propaganda</title></head><body><p>Visit us at <a href=\"http:// clojureprogramming.com\">our website</a>.</p></body></html>
(html (list* :ul (for [author ["Chas Emerick" "Christophe Grand" "Brian Carper"]]
                   [:li author])))
;"<ul><li>Chas Emerick</li><li>Christophe Grand</li><li>Brian Carper</li></ul>"

;使用断言来让代码快速失败
(defn attrs
  [attr-map]
  (assert (or (map? attr-map)
              (nil? attr-map))
          "attr-map must be nil,or a map")
  (->> attr-map
       (mapcat (fn [[k v]] [\space (name k) "=\"" v "\""]))
       (apply str)))

;但断言抛出的是Error,会导致程序停止,所以在生产环境你可以禁用断言
(set! *assert* false)
(defn attrs
  [attr-map]
  (assert (or (map? attr-map)
              (nil? attr-map))
          "attr-map must be nil,or a map")
  (->> attr-map
       (mapcat (fn [[k v]] [\space (name k) "=\"" v "\""]))
       (apply str)))
(try (attrs "hi")
     (catch Exception e
       (.getMessage e)))
(set! *assert* true)
;nth not supported on this type: Character

;断言的最常用的例子是作为前置条件和后置条件
;clojure的函数体如果第一个参数是带有:pre和:post映射,这个映射会被看做前后条件表达式的映射
;它在编译的时候会转换成对assert的调用.但如果这个映射是函数体的唯一表达式,那么这个映射会视为函数的返回值
;:pre和:post的值是vector,其中的每个元素都被看做是一个断言
;函数的参数可以在前置条件中被引用,函数的返回值在后置条件中和%绑定
;现在使用前后置条件来重写attrs和html
(defn attrs
  [attr-map]
  {:pre [(or (map? attr-map)
             (nil? attr-map))]}
  (->> attr-map
       (mapcat (fn [[k v]] [\space (name k) "=\"" v "\""]))
       (apply str)))
(defn html
  [x]
  {:pre [(if (sequential? x)
           (some #(-> x first %) [keyword? symbol? string?])
           (not (map? x)))]
   :post [(string? %)]}
  (if-not (sequential? x)
    (str x)
    (let [[tag & body] x
          [attr-map body] (if (map? (first body))
                            [(first body) (rest body)]
                            [nil body])]
      (str "<" (name tag) (attrs attr-map) ">"
           (apply str (map html body))
           "</" (name tag) ">"))))
(html {:tag :a})
;java.lang.AssertionError: Assert failed: (if (sequential? x) (some (fn* [p1__3388#] (-> x first p1__3388#)) [keyword? symbol? string?]) (not (map? x)))

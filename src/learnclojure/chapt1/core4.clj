(ns learnclojure.chapt1.core4
  (:import (java.util Date)))
;;map解构,对以下数据解构有效
;;1.clojure原生的array-map,hash-map,以及记录类型
;;2.任何实现了java.util.Map的对象
;;3.get方法所支持的对象，如clojure原生的vector，字符串，数组
(def m {:a 5 :b 6
        :c [7 8 9]
        :d {:e 10 :f 11}
        "foo" 88
        42 false})
(let [{a :a b :b } m]
  (+ a b))
(let [{f "foo"} m]
  (+ f 12))
(let [{v 42} m]
  (if v 1 0))
;;如果进行map解构的是get所支持的对象，则解构的key是数字类型的数组下标
(let [{x 3 y 8} [12 0 0 -18 44 6 0 0 1]]
  (+ x y))
(let [{{e :e} :d} m]
  (* e 2))
;;可以把顺序解构和map解构结合起来
(let [{[x y z ] :c} m]
  (+ x y z))
(def map-in-vector ["James" {:birthday (Date. 73 1 4)}])
(let [[ name {day :birthday}] map-in-vector]
  (str name " is borned on " day))
;;map解构的额外特性
;;和顺序解构一样，可以用:as保持被解构的值
(let [{r1 :x r2 :y :as randoms}
      (zipmap [:x :y :z] (repeatedly #(rand-int 10)))]
  (assoc randoms :sum (+ r1 r2)))
;;可以用:or 提供一个默认的map，如果要解构的key在集合中没有的话，可以用这个默认map中的值
(let [{k :unknown x :a
       :or {k 50}} m]
  (+ k x))
;;:or可以区分有没有赋值还是赋的是nil或false
(let [{opt1 :option} {:option false}
      opt1 (or opt1 true)
      {opt2 :option :or {opt2 true}} {:option false}]
      {:opt1 opt1,:opt2 opt2})
;;绑定符号到map中同名关键字所对应的值，如果按之前的写法会很冗长，不符合clojure简洁的原则
;;所以，clojure提供了:keys、:strs、:syms
;;其中:keys比其他两个用的多的多
(def zjj {:name "zjj" :age 25 :location "Suzhou"} )
(let [{:keys [name age location]} chas]
  (format "%s is %s years old and lives in %s" name age location))
(def shi {"name" "shi" "age" 12 "location" "ShangHai"} )
(let [{:strs [name age location]} shi]
  (format "%s is %s years old and lives in %s" name age location))
(def yang {'name "yang" 'age 54 'location "BeiJing"})
(let [{:syms [name age location]} yang]
  (format "%s is %s years old and lives in %s" name age location))
(def user-info ["robot" 2011 :name "Bob" :city "Boston"])
(let [[username year & {:keys [name city]}]  user-info]
  (format "%s is in %s " name city))

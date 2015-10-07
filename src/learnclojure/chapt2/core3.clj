(ns learnclojure.chapt2.core3
  (:import (java.util Date)))
;;高阶函数
;;一个返回摸个给定数字与它的参数的和的函数
(defn adder
  [n]
  (fn [x]
    (+ n x)))
((adder 5) 1)
;;接受一个函数做参数，返回一个函数的返回值的两倍
(defn doubler
  [f]
  (fn [& args]
    (* 2 (apply f args))))
(def double-+ (doubler +))
(double-+ 1 2 4)

;;实现一个简单的日志系统
;;一个高阶函数，返回一个函数绑定一个输出的实例到*out*,并可以对打印传入的信息
(defn print-logger
  [writer]
  #(binding [*out* writer]
    (println %)))
;;把输入的消息打印到标准输出
(def *out*-logger (print-logger *out*))
(*out*-logger "hello")
;;把消息打印到一个内存buffer,使用java.io.StringWriter
(def writer (java.io.StringWriter.))
(def retained-logger (print-logger writer))
(retained-logger "hehe")
(str writer)
;;输出信息到文件
;;file可以是文件路径，也可以是java.net.URL/URI
;;with-open保证f在with-open结束的时候被关闭，append=true表示file以追加模式打开
(require 'clojure.java.io)
(defn file-logger
  [file]
  #(with-open [f (clojure.java.io/writer file :append true)]
    ((print-logger f) %)))
(def log->file (file-logger "message.log"))
(log->file "qeqe")
;;如果需要有多个输出,定义一个高阶函数遍历传入的多个logger并输出
(defn muti-logger
  [& loggers]
  #(doseq [f loggers]
    (f %)))
(def log (muti-logger *out*-logger log->file))
(log "nn")
;;在日志前加上时间戳
(defn timestamped-logger
  [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s" (Date.) %)))
(def log-timestamped (timestamped-logger log))
(log-timestamped "pp")
(require 'clojure.xml)

;;纯函数,不会改变外部的某些属性，也不依赖外部的环境
;;下面的函数请求雅虎的天气api查询天气，会随着时间变化查询出不同结果，所以不是纯函数
(->> (str "http://weather.yahooapis.com/forecastrss?w=2151330&u=c")
     clojure.xml/parse
     :content
     print)
;;纯函数优点
;;1.应为对于给定的出入，返回值是确定的，所以便于测试
;;2.便于缓存和并行化，因为任何使用纯函数的地方可以直接用它的返回值代替
;;所以可以把返回值缓存起来,下次调用这个函数的时候可以直接返回这个返回值,这个叫内存化



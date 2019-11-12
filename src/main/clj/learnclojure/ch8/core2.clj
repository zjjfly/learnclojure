;以上的函数都是在repl中使用的,在实际项目中一般在ns宏中通过参数配置
(ns learnclojure.ch8.core2
  (:refer-clojure :exclude [next replace remove])           ;把clojure.core中的一些var排除,因为和clojure.zip中的冲突了
  (:require (clojure [string :as str]
                     [set as :set])
            [clojure.java.shell :as sh])
  (:use (clojure zip xml))
  (:import java.util.Date
           java.text.SimpleDateFormat
           (java.util.concurrent Executors
                                 LinkedBlockingDeque)))


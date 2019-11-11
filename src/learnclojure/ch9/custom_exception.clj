(ns learnclojure.ch9.custom-exception
  (:gen-class :extends RuntimeException
              :name learnclojure.ch9.CustomException
              :implements [clojure.lang.IDeref]
              :constructors {[java.util.Map String]           [String]
                             [java.util.Map String Throwable] [String Throwable]}
              :init init
              :state info
              :methods [[getInfo [] java.util.Map]
                        [addInfo [Object Object] void]]))
(import 'learnclojure.ch9.CustomException)
(defn- -init
  ([info message]
   [[message] (atom (into {} info))])
  ([info message ex]
   [[message ex] (atom (into {} info))]))

(defn- -deref
  [^CustomException this]
  @(.info this))

(defn- -getInfo
  [^CustomException this]
  @this)

(defn- -addInfo
  [^CustomException this key value]
  (swap! (.info this) assoc key value))

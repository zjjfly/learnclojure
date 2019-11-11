(ns learnclojure.ch9.annotation
  (:import (org.junit Test Assert))
  (:gen-class
    :name learnclojure.ch9.JunitTest
    :methods [[^{org.junit.Test true} simpleTest [] void]
              [^{org.junit.Test {:timeout 2000}} timeoutTest [] void]
              [^{org.junit.Test {:expected NullPointerException}} badException [] void]]))

(defn -simpleTest
  [this]
  (Assert/assertEquals (class this) learnclojure.ch9.JunitTest))

(defn -badException
  [this]
  (Integer/parseInt (System/getProperty "nonexistent")))

(defn -timeoutTest
  [this]
  (Thread/sleep 10000))

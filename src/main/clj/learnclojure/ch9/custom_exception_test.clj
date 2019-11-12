(ns learnclojure.ch9.custom-exception-test
  (:import (learnclojure.ch9 BatchJob)
           (learnclojure.ch9 CustomException)))

(try
  (BatchJob/runBatchJob 123)
  (catch CustomException e
    (println "Error! " (.getMessage e) @e)))

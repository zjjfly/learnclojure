(ns learnclojure.ch9.classes)

(deftype Range
         [start end]
  Iterable
  (iterator [this]
    (.iterator (range start end))))

(defn string-range
  [start end]
  (Range. (Long/parseLong start) (Long/parseLong end)))

(defrecord OrderSummary
           [order-number total])

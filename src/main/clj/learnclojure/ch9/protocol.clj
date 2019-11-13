(ns learnclojure.ch9.protocol)

(defprotocol Talkable
  (speak [this]))

(extend-protocol Talkable
  String
  (speak [s] s)
  Object
  (speak [this]
    (str (-> this class .getName) "s can't talk!")))

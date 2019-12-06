(ns learnclojure.ch14.hibernate
  (:import (org.hibernate.cfg Configuration)
           (org.hibernate SessionFactory)
           (learnclojure.ch14 Author)))

(defonce session-factory
         (delay (-> (Configuration.)                        ;加上delay保证在提前编译的时候不会执行这个代码
                    .configure
                    .buildSessionFactory)))

(defn add-authors
  [& authors]
  (with-open [session (.openSession ^SessionFactory @session-factory)]
    (let [tx (.beginTransaction session)]
      (doseq [author authors]
        (.save session author))
      (.commit tx))))

(add-authors (Author. "Kexin" "Zi"))

(defn get-authors
  []
  (with-open [session (.openSession ^SessionFactory @session-factory)]
    (-> session
        (.createQuery "from Author")
        .list)))

(for [{:keys [firstName lastName]} (map bean (get-authors))]
  (str lastName "," firstName))

;写一个宏来避免重复写生成session的代码
(defmacro with-session
  [session-factory & body]
  `(with-open [~'session (.openSession ~(vary-meta session-factory assoc
                                                   :tag 'SessionFactory))]
     ~@body))

;使用这个宏来重新实现get-authors
(defn get-authors
  []
  (with-session @session-factory
                (-> session
                    (.createQuery "from Author")
                    .list)))

;再写一个宏来避免写事务相关的代码
(defmacro with-transaction
  [& body]
  `(let [~'tx (.beginTransaction ~'session)]
     ~@body
     (.commit ~'tx)))

;使用这两个宏来重新实现add-authors
(defn add-authors
  [& authors]
  (with-session @session-factory
   (with-transaction
     (doseq [author authors]
       (.save session author)))))

(add-authors (Author. "Jun Jie" "Zi"))

(ns learnclojure.pop_lib.korma_mysql
  (:refer-clojure :exclude [update])
  (:use korma.core
        korma.db))
(defdb mysql-db
  (mysql {:db "test?useSSL=false" :host "localhost" :port "3306" :user "root" :password "123456"}))
(defentity user
  (table :user)
  (database mysql-db))
(defn save-user! []
  (insert user
          (values {:age 28, :name "jjzi", :sex 1})))
(defn select-user []
  (select user
          (where {:name "jjzi"})
          (order :age :desc)))
(select-user)

(ns learnclojure.ch14.jdbc
  (:require [clojure.java.jdbc :as jdbc])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def db-spec
  {:classname "com.mysql.cj.jdbc.Driver"
   :dbtype   "mysql"
   :dbname   "test"
   :host     "localhost"
   :user     "root"
   :password "123456"})

(jdbc/with-db-connection
  [conn db-spec]
  (jdbc/execute! conn
                 [(jdbc/create-table-ddl :authors [[:id :serial "PRIMARY KEY"]
                                                   [:first_name "VARCHAR(20)"]
                                                   [:last_name "VARCHAR(20)"]]
                                         {:conditional? true} ;是否在语句中加入if not exists
                                         )]))

;插入多行,返回自动生成的key.实际的实现是一行行插入的,所以性能一般
(jdbc/with-db-connection
  [conn db-spec]
  (jdbc/insert-multi! conn :authors [{:first_name "Chas" :last_name "Emerick"}
                                     {:first_name "Christophe" :last_name "Grand"}
                                     {:first_name "Brian" :last_name "Carper"}]))

;批量插入多行,返回的是一个受影响的行数的序列.性能较高
(jdbc/with-db-connection
  [conn db-spec]
  (jdbc/insert-multi! db-spec :authors [:first_name :last_name] [["Junjie" "Zi"]
                                                                 ["Jiali" "Chen"]]))

;查询
(jdbc/query db-spec ["select * from authors where last_name = ?" "Zi"])
;({:id 7, :first_name "Junjie", :last_name "Zi"})

;事务
(jdbc/with-db-transaction
  [conn db-spec]
  (.setTransactionIsolation (:connection conn)              ;设定事务的隔离水平
                  java.sql.Connection/TRANSACTION_SERIALIZABLE)
  (jdbc/delete! conn :authors ["last_name = ?" "Zi"])
  (throw (Exception. "Abort transaction!")))

;连接池
(def pooled-spec
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass "com.mysql.cj.jdbc.Driver")
                 (.setJdbcUrl "jdbc:mysql://localhost:3306/test")
                 (.setUser "root")
                 (.setPassword "Zjj123!@#"))})
(jdbc/query pooled-spec ["select * from authors where last_name = ?" "Zi"])
;({:id 7, :first_name "Junjie", :last_name "Zi"})

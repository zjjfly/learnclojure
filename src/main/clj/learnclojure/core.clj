(ns learnclojure.core
  (:gen-class)
  (:use [learnclojure.utils.string_util])
  (:require [toucan.db :as db]
            [toucan.models :as models]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.api.sweet :refer [api routes]]
            [learnclojure.pop_lib.user :refer [user-routes]]))

(def db-spec
  {:dbtype   "mysql"
   :dbname   "test"
   :user     "root"
   :password "123456"})

(def swagger-config
  {:ui      "/swagger"
   :spec    "/swagger.json"
   :options {:ui   {:validatorUrl nil}
             :data {:info {:version "1.0.0", :title "Restful CRUD API"}}}})

; (def app (apply routes user-routes))
(def app (api {:swagger swagger-config} (apply routes user-routes)))

(defn -main
  [& args]
  (db/set-default-db-connection! db-spec)
  (db/set-default-quoting-style! :mysql)
  (models/set-root-namespace! 'learnclojure.pop_lib.models)
  (run-jetty app {:port 3000}))

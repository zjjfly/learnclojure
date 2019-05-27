(defproject learnclojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ; Web
                 [prismatic/schema "1.1.9"]
                 [metosin/compojure-api "2.0.0-alpha26"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 ; Database
                 [toucan "1.1.9"]
                 [org.postgresql/postgresql "42.2.4"]
                 ; Password Hashing
                 [buddy/buddy-hashers "1.3.0"]
                 [korma "0.4.3"]
                 [mysql/mysql-connector-java "5.1.42"]
                 [org.clojure/core.memoize "0.7.1"]
                 [enlive "1.1.6"]
                 [junit/junit "4.12"]]
  :main ^:skip-aot learnclojure.core
  :plugins [[lein-cljfmt "0.5.7"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot [learnclojure.core]}}
  ;:aot :all
  :global-vars {*warn-on-reflection* true}
  :java-source-paths ["java"])

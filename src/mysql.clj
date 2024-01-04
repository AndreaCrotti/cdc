(ns mysql
  (:require
   [honeysql.helpers :as hh]
   [honeysql.core :as h]
   [tick.core :as t]
   [next.jdbc :as j]
   [next.jdbc.sql :as js]))


(defn config->dbspec [{:keys [root-password root-user db] :as _config} port]
  {:dbtype   "mysql"
   :host     "localhost"
   :user     root-user
   :port     port
   :password root-password
   :dbname   db})

(defn update-versions [version]
  (-> (hh/update :big_table)
      (hh/sset {:version version, :updated_at (t/now)})
      (h/format {:pretty true})))

(defn create-table! [ds]
  (j/execute! ds
              ["CREATE TABLE IF NOT EXISTS big_table (id INTEGER PRIMARY KEY, version INTEGER, text TEXT, created_at TIMESTAMP, updated_at TIMESTAMP)"]))

(defn insert-data! [ds version]
  (let [data (for [n (range 1000)]
               {:id n
                :text (str "record: " n)
                :created_at (t/now)
                ;; add created at timestamp
                :version version})]
    (js/insert-multi! ds
                      "big_table"
                      data)
    data))

(defn update-data! [ds version]
  (j/execute! ds (update-versions version)))

(defn get-rows [ds]
  (js/query ds ["SELECT * FROM big_table"]))

(defn get-ds [config port]
  (let [db-spec (config->dbspec config port)]
    (j/get-datasource db-spec)))

(comment
  (require '[config :refer [configuration]])
  (def m (:mysql configuration))
  (def db-spec (config->dbspec m 32872))
  (def ds (j/get-datasource db-spec))
  (j/execute! ds ["SELECT 1"])
  (create-table! ds)
  )

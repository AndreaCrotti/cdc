(ns mysql
  (:require
   [next.jdbc :as j]
   [next.jdbc.sql :as js]))


(defn config->dbspec [{:keys [root-password root-user db] :as _config} port]
  {:dbtype   "mysql"
   :host     "localhost"
   :user     root-user
   :port     port
   :password root-password
   :dbname   db})

(defn create-table! [ds]
  (j/execute! ds
              ["CREATE TABLE big_table (id INTEGER PRIMARY KEY, text TEXT, created_at TIMESTAMP)"]))

(defn insert-data [ds]
  (let [data (for [n (range 1000)]
               {:id n
                :text (str "record: " n)
                ;; add created at timestamp
                })]
    (js/insert-multi! ds
                      "big_table"
                      data)
    data))

(defn seed! [ds]
  (create-table! ds)
  (insert-data ds))

(defn get-rows [ds]
  (js/query ds ["SELECT * FROM big_table"]))

(defn get-ds [config port]
  (let [db-spec (config->dbspec config port)]
    (j/get-datasource db-spec)))

(comment
  (require '[config :refer [configuration]])
  (def m (:mysql (configuration)))
  (def db-spec (config->dbspec m 32872))
  (def ds (j/get-datasource db-spec))
  (j/execute! ds ["SELECT 1"])
  (create-table! ds)
  )

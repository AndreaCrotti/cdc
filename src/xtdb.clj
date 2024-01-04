(ns xtdb
  (:require
   [cheshire.core :as json]
   [integrant.core :as ig]
   [xtdb.api :as xt]))

(defn payload->doc [{:keys [after ts_ms op] :as payload}]
  (-> {:xt/id (:id after)}
      (merge after)
      (dissoc :id)))

(defn is-row? [payload]
  (some? (:after payload)))

(defn add-record! [node record]
  (let [payload (-> (.value record)
                    (json/parse-string keyword)
                    :payload)]
    (when (is-row? payload)
      (let [doc (payload->doc payload)]
        ;; get a full example of a record to see all the other fields available
        (println "inserting document " doc)
        (try
          (println
           "transaction  -> "
           (xt/await-tx
            node
            (xt/submit-tx node [[::xt/put (payload->doc payload)]])))
          (catch Exception e
            (println "got exception " e)))))))

(defmethod ig/init-key ::node [_ _]
  (xt/start-node {}))

(defmethod ig/halt-key! ::node [_ node]
  ;; don't need to do anything really
  (println "closing node" node))

(comment
  (def node (xt/start-node {}))
  (def db (xt/db node))
  (def sample-record
    {:xt/id :other-record
     :key   "some-key"
     :value 42})

  (xt/await-tx
   node
   (xt/submit-tx node [[::xt/put sample-record]]))

  #_(xt/sync node)

  (xt/entity (xt/db node) :other-record)

  )

(ns debezium
  (:require
   [config :refer [configuration]]
   [integrant.core :as ig]
   [xtdb :as x])
  (:import
   (io.debezium.config Configuration)
   (io.debezium.embedded EmbeddedEngineChangeEvent)
   (io.debezium.engine DebeziumEngine)
   (io.debezium.engine.format Json)
   (java.util.concurrent Executors)
   (java.util.function Consumer)))

(defn event-consumer
  "Consumer that handles events from Debezium."
  [{:keys [node]} ^EmbeddedEngineChangeEvent record]
  ;; Process the `SourceRecord` received from Debezium here.
  ;; For example, turn it into EDN, map to your domain, etc.
  (x/handle-op! node record))


(defn config [port {:keys [root-password root-user]}] ;; using the ``root-password`` to avoid having to add extra permission to the standard user
  (-> (Configuration/empty)
      (.edit)
      ;; TODO: can we just move the config to a map and generate this
      ;; object accordingly?
      (.with "name" "engine")
      (.with "connector.class" "io.debezium.connector.mysql.MySqlConnector")
      (.with "offset.storage" "org.apache.kafka.connect.storage.FileOffsetBackingStore")
      ;; make the path configurable?
      (.with "offset.storage.file.filename" (-> configuration :kafka :offsets))
      (.with "offset.flush.interval.ms" "60000")
      (.with "topic.prefix" "my-app-connector")

      ;; not sure wat this is for
      (.with "database.server.id" (str port))
      (.with "database.server.name" "localhost")
      (.with "database.hostname" "localhost")
      (.with "database.port" port)
      (.with "database.password" root-password)
      (.with "database.user" root-user)
      (.with "schema.history.internal" "io.debezium.storage.file.history.FileSchemaHistory")
      ;; change this path as well??
      (.with "schema.history.internal.file.filename" (-> configuration :kafka :schema-history))
      (.build)
      (.asProperties)))

(defn start-debezium
  "Starts the Debezium engine with a MySQL connector."
  [{:keys [mysql] :as ctx}]
  (let [port (:port mysql)
        c (config port (:mysql configuration))
        ^Consumer consumer
        (reify Consumer
          (accept [_ record]
            (event-consumer ctx record)))
        engine
        ;; `Json` is the format we are picking to get the data in
        (-> (DebeziumEngine/create Json)
            (.using c)
            (.notifying consumer)
            (.build))
        executor (Executors/newSingleThreadExecutor)]

    (.execute executor engine)
    engine))


(defmethod ig/init-key ::engine [_ ctx]
  (start-debezium ctx))

(defmethod ig/halt-key! ::engine [_ engine]
  (println "closing debezium engine")
  (.close engine))

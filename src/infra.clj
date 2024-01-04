(ns infra
  (:require
   [config :refer [configuration]]
   [clj-test-containers.core :as tc]
   [integrant.core :as ig]))

(defn mysql []
  (let [{:keys [db user password root-password]} (-> (configuration) :mysql)]
    (-> (tc/create {:image-name    "mysql:8.2.0"
                    :log-to        {:log-strategy :string}
                    :wait-for      {:wait-strategy :port}
                    :exposed-ports [3306]
                    :env-vars      {"MYSQL_USER"          user
                                    "MYSQL_DATABASE"      db
                                    "MYSQL_PASSWORD"      password
                                    "MYSQL_ROOT_PASSWORD" root-password}})
        (tc/bind-filesystem! {:host-path      "/tmp"
                              :container-path "/opt"
                              :mode           :read-only}))))

(defn start! [container]
  (let [started (tc/start! container)
        port    (-> started :exposed-ports first)]
    {:port      (get (:mapped-ports started) port)
     :container started}))

(defmethod ig/init-key ::mysql
  [_ _]
  (start! (mysql)))

(defmethod ig/halt-key! ::mysql
  [_ {:keys [container]}]
  (tc/stop! container))

(ns infra
  (:require
   [config :refer [configuration]]
   [clj-test-containers.core :as tc]
   [integrant.core :as ig]))


(defn postgres []
  (let [{:keys [postgresql]} (configuration)]
    (-> (tc/create {:image-name    "postgres:15.3"
                    :wait-for      {:wait-strategy :port}
                    :exposed-ports [5432]
                    :env-vars      {"POSTGRES_USER"        (:user postgresql)
                                    "POSTGRES_PASSWORD"    (:password postgresql)
                                    "POSTGRES_DB"          (:db postgresql)}})
        #_(tc/bind-filesystem! {:host-path      "/tmp"
                                :container-path "/opt"
                                :mode           :read-only})

        #_(tc/start!))))

(defn mysql []
  (let [{:keys [mysql]} (configuration)]
    (-> (tc/create {:image-name    "mysql:8.2.0"
                    :log-to        {:log-strategy :string}
                    :wait-for      {:wait-strategy :port}
                    :exposed-ports [3306]
                    :env-vars      {"MYSQL_USER"          (:user mysql)
                                    "MYSQL_DATABASE"      (:db mysql)
                                    "MYSQL_PASSWORD"      (:password mysql)
                                    "MYSQL_ROOT_PASSWORD" (:root-password mysql)}})
        (tc/bind-filesystem! {:host-path      "/tmp"
                              :container-path "/opt"
                              :mode           :read-only})
        #_(tc/start!))))

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

(ns debezium-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [diehard.core :as die]
   [integrant.repl.state :refer [system]]
   [mysql :as m]
   [system :as s]
   [xtdb.api :as xt]))

(defn clear-files! []
  (io/delete-file "/tmp/offsets.dat" true)
  (io/delete-file "/tmp/schemahistory.dat" true))

(deftest msyql->xtdb-test
  (testing "Can sync data from one db to the other"
    ;; start the whole system first
    (clear-files!)
    (s/start-all)
    (try
      (let [port   (-> system :infra/mysql :port)
            config (-> system :config/configuration :mysql)
            ds     (m/get-ds config port)
            node   (-> system :xtdb/node)
            data   (m/seed! ds)]

        ;; would be nice to avoid this sleep, but the retry alone is not enough
        (Thread/sleep 3000)
        ;; now check that the data is in XTDB
        (die/with-retry
            {:max-retries 100
             :delay-ms    100}
          (is (= {:text "record: 0", :created_at nil, :xt/id 0}
                 (xt/entity (xt/db node) 0)))))
      (finally
        (s/stop-all)))))

(ns debezium-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [diehard.core :as die]
   [integrant.repl.state :refer [system]]
   [config :refer [configuration]]
   [mysql :as m]
   [system :as s]
   [xtdb.api :as xt]
   [tick.core :as t]))

(defn clear-files! []
  (io/delete-file (-> configuration :kafka :offsets) true)
  (io/delete-file (-> configuration :kafka :schema-history) true))

;; what kind of data do I want to enter and query back?

(deftest msyql->xtdb-test
  (testing "Can sync data from one db to the other"
    ;; start the whole system first
    (clear-files!)
    (s/start-all)
    (try
      (let [port   (-> system :infra/mysql :port)
            config (-> configuration :mysql)
            ds     (m/get-ds config port)
            node   (-> system :xtdb/node)]

        ;; do a first seeding of the data
        (m/create-table! ds)
        (m/insert-data! ds 1)
        ;; would be nice to avoid this sleep, but the retry alone is not enough
        (Thread/sleep 3000)
        ;; now check that the data is in XTDB
        (die/with-retry
            {:max-retries 100
             :delay-ms    100}
          (is (= {:text "record: 0", :xt/id 0, :version 1}
                 ;; use the entity history here
                 (-> (xt/entity (xt/db node) 0)
                     ;; don't need to check the timestamps for now
                     (select-keys [:text :xt/id :version])))))
        ;; now seed again to see if there are multiple versions
        (is (= 1 (count (xt/entity-history (xt/db node) 0 :asc))))
        (m/update-data! ds 2)
        ;; try to avoid sleeps if possible
        (Thread/sleep 3000)
        (is (= 2 (count (xt/entity-history (xt/db node) 0 :asc))))

        ;; now what happens if we delete everything??

        (m/delete-record! ds 0)
        (Thread/sleep 3000)
        ;; now check that the value is deleted unless you go back in history
        (is (empty? (xt/entity-history (xt/db node) 0 :asc))))
      (finally
        (s/stop-all)))))

;; write the same value multiple times and see the different content?

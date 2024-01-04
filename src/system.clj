(ns system
   (:require
    [debezium :as d]
    [infra :as infra]
    [integrant.core :as ig]
    [integrant.repl :as ir]
    [nrepl :as n]
    [xtdb :as x]))


(def config
  {::x/node          {}
   ::n/nrepl-server  {:port 5000}
   ::infra/mysql     {}
   ::d/engine
   {:node          (ig/ref ::x/node)
    :mysql         (ig/ref :infra/mysql)}})


(defn start-all []
  (ir/set-prep! #(ig/prep config))
  (ir/prep)
  (ir/init))

(defn stop-all []
  (ir/halt))

(ns system
   (:require
    [integrant.core :as ig]
    [integrant.repl :as ir]
    [nrepl :as n]
    [infra :as infra]
    [debezium :as d]
    [xtdb :as x]
    [config :as c]))


(def config
  {::x/node          {}
   ::n/nrepl-server  {:port 5000}
   ::infra/mysql     {}
   ::c/configuration {}
   ::d/engine
   {:configuration (ig/ref ::c/configuration)
    :node          (ig/ref ::x/node)
    :mysql         (ig/ref :infra/mysql)}})


(defn start-all []
  (ir/set-prep! #(ig/prep config))
  (ir/prep)
  (ir/init))

(defn stop-all []
  (ir/halt))

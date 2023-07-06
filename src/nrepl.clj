(ns nrepl
  (:require
   [integrant.core :as ig]
   [nrepl.server :as nrepl]))

(defmethod ig/init-key ::nrepl-server [_ {:keys [port]}]
  (assert (some? port), "Missing port")
  (println "Starting nrepl server on port " port)
  (nrepl/start-server :port port
                      :bind "0.0.0.0"))


(defmethod ig/halt-key! ::nrepl-server [_ repl]
  (nrepl/stop-server repl))

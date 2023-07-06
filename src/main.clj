(ns main
  (:require
   [clojure.tools.logging :as log]
   [system :as s]))

(defn -main [& _args]
  (log/info "Info logging")
  (log/debug "debug logging")
  (log/warn "warn logging")

  (s/start-all))

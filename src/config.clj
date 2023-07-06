(ns config
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [aero.core :as a]))


(defn configuration []
  ;; should be able to use `io/resource` in theory if everything is set up correctly
  (a/read-config (io/file "resources/config.edn")))

(defmethod ig/init-key ::configuration
  [_ _]
  (configuration))

(defmethod ig/halt-key! ::configuration
  [_ _]
  ;; nothing needed
  )

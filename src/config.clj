(ns config)

(def configuration
  {:mysql
   {:password      "verysecret"
    :user          "user"
    :root-password "root-pw"
    :root-user     "root"
    :db            "sample_db"}
   :kafka
   {:schema-history "/tmp/schemahistory.dat"
    :offsets        "/tmp/offsets.dat"}})

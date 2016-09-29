(ns fivethreeonern.sqlite
  (:require [mount.core :refer-macros [defstate]]))

(def node-module (js/require "react-native-sqlite-storage"))

(defstate sqlite
  :start (.openDatabase node-module #js {:name "531.db" :location "default"} #(js/console.log "sql ok") #(js/console.log "sql error")))

(defn execute-sql [tx final-cb [query & other-queries]]
  (.executeSql tx query #js [] (fn [tx results]
                                 (if (empty? other-queries)
                                   (let [results (-> results .-rows .raw js->clj)]
                                     (final-cb results))
                                   (execute-sql tx final-cb other-queries)))))

(defn transaction [query-strings final-cb]
  (.transaction @sqlite
                (fn [tx]
                  (execute-sql tx final-cb query-strings))))

(defn query [query-str cb]
  (transaction [query-str] cb))

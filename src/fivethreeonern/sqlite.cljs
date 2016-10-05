(ns fivethreeonern.sqlite
  (:require [mount.core :refer-macros [defstate]]))

(def node-module (js/require "react-native-sqlite-storage"))

(defstate sqlite
  :start (.openDatabase node-module #js {:name "531.db" :location "default"} #(js/console.log "sql ok") #(js/console.log "sql error")))

(defn- execute-sql [tx [query & other-queries] final-cb on-error]
  (.executeSql tx query #js []
               (fn [tx results]
                 (if (empty? other-queries)
                   (let [results (-> results .-rows .raw js->clj)]
                     (final-cb results))
                   (execute-sql tx other-queries final-cb on-error)))
               on-error))

(defn transaction
  ([query-strings final-cb]
   (transaction query-strings final-cb (fn [_])))
  ([query-strings final-cb on-error]
   (.transaction @sqlite
                 (fn [tx]
                   (execute-sql tx query-strings final-cb on-error)))))

(defn query
  ([query-str cb]
   (query query-str cb (fn [_])))
  ([query-str cb on-error]
   (transaction [query-str] cb on-error)))

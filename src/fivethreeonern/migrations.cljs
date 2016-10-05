(ns fivethreeonern.migrations
  (:require [fivethreeonern.sqlite :refer [query]]
            [goog.string :as gstring]))

(def migrations ["CREATE TABLE IF NOT EXISTS test_migrations (id INTEGER);"])

(declare run-migrations)

(defn last-migration-id
  [cb]
  (query "SELECT id FROM migrations ORDER BY id"
         (fn [result]
           (-> result last :id cb))))

(defn insert-migration
  [id rest-migrations cb]
  (query (gstring/subs "INSERT INTO migrations (id) VALUES (%s);" id)
         (fn [_]
           (if (empty? rest-migrations)
             (cb)
             (run-migrations rest-migrations cb)))
         (fn [error]
           (js/console.log error))))

(defn run-migrations
  [[[id migration] & rest-migrations] cb]
  (js/console.log "running migration id" id)
  (query migration (fn [_]
                     (insert-migration id rest-migrations cb))))

(defn migrate
  [cb]
  (last-migration-id
   (fn [last-migration-id]
     (js/console.log last-migration-id)
     (let [to-migrate-count (or last-migration-id 0)]
       (run-migrations (map-indexed vector
                                    (drop to-migrate-count migrations))
                       cb)))))

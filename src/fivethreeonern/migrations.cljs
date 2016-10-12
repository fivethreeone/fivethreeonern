(ns fivethreeonern.migrations
  (:require [fivethreeonern.sqlite :refer [query]]
            [goog.string :as gstring]))

(def migrations ["CREATE TABLE IF NOT EXISTS test_migrations (id INTEGER);"])

(declare run-migrations)

(defn ensure-migrations-table! [cb]
  (query "CREATE TABLE IF NOT EXISTS migrations (id INTEGER);"
         (fn [_]
           (cb))))

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
           (js/console.error error))))

(defn run-migrations
  [[[id migration] & rest-migrations] cb]
  (js/console.log "running migration id" id migration)
  (query migration (fn [_]
                     (insert-migration id rest-migrations cb))))

(defn migrate
  [cb]
  (letfn [(migrate-to-id [migration-id]
            (let [migrations-to-drop (or (inc migration-id) 0)
                  migrations-to-run (drop migrations-to-drop migrations)]
              (if (empty? migrations-to-run)
                (cb)
                (run-migrations (map vector
                                     (drop migrations-to-drop (range))
                                     migrations-to-run)
                                cb))))]
    (ensure-migrations-table!
     (fn []
       (last-migration-id migrate-to-id)))))

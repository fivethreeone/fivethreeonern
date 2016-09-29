(ns fivethreeonern.migrations
  (:require [fivethreeonern.sqlite :refer [query]]
            [goog.string :as gstring]))

(def migrations ["CREATE TABLE IF NOT EXISTS test_migrations (id INTEGER);"])

(declare run-migrations)

(defn last-migration-id
  []
  (query "SELECT id FROM migrations ORDER BY id" (comp :id last)))

(defn insert-migration
  [id rest-migrations]
  (query (gstring/subs "INSERT INTO migrations (id) VALUES (%s);" id)
         (fn [_]
           (if (empty? rest-migrations)
             (prn "Migrations succeeded!")
             (run-migrations rest-migrations)))))

(defn run-migrations
  [[[id migration] & rest-migrations]]
  (query migration (fn [_]
                     (insert-migration id rest-migrations))))

(defn migrate
  [_]
  (let [to-migrate-count (or (last-migration-id) 0)]
    (run-migrations (map-indexed vector (drop to-migrate-count migrations)))))
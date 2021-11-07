(ns subscribers-bot.data
  (:require [clojure.java.jdbc :as jdbc])
  (:gen-class)
  (:import (java.util UUID)))

(defn uuid [] (.toString (UUID/randomUUID)))

(defn db [db-file-path]
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     db-file-path
   })

(defn create-users-table
  "create db and table"
  [db]
  (try (jdbc/db-do-commands db
                            (jdbc/create-table-ddl :users
                                                   [[:id "varchar(36)" :primary :key]
                                                    [:timestamp :datetime :default :current_timestamp]
                                                    [:telegram_id :long :key :unique]
                                                    [:name :text]] {:conditional? true}))
       (catch Exception e
         (println (.getMessage e)))))

(defn create-report-requests
  "create db and table"
  [db]
  (try (jdbc/db-do-commands db
                            (jdbc/create-table-ddl :report_requests
                                                   [[:id "varchar(36)" :primary :key]
                                                    [:timestamp :datetime :default :current_timestamp]
                                                    [:user_id "varchar(36)"]
                                                    [:site "varchar(10)"]] {:conditional? true}))
       (catch Exception e
         (println (.getMessage e)))))

(defn get-user-by-telegram-id
  [db telegram-id]
  (jdbc/query db ["select * from active_users where telegram_id = ?" telegram-id]))

(defn create-user [db name telegram-id]
  (jdbc/insert! db :users {:id (uuid) :name name :telegram_id telegram-id}))

;insert! db :news testdata

(ns subscribers-bot.data
  (:require [clojure.java.jdbc :as jdbc])
  (:gen-class)
  (:import (java.util UUID)))

(defn uuid [] (.toString (UUID/randomUUID)))

(defn database [db-file-path]
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     db-file-path
   })

(def db-file-path (or (System/getenv "DB_FILE_PATH") "local.db"))

(def db (database db-file-path))

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

(defn create-report-requests-table
  "create db and table"
  [db]
  (try (jdbc/db-do-commands db
                            (jdbc/create-table-ddl :report_requests
                                                   [[:id "varchar(36)" :primary :key]
                                                    [:timestamp :datetime :default :current_timestamp]
                                                    [:user_id "varchar(36)"]
                                                    [:site_user_id "varchar(36)"]
                                                    [:site "varchar(10)"]] {:conditional? true}))
       (catch Exception e
         (println (.getMessage e)))))

(defn create-subscribers-table
  "create db and table"
  [db]
  (try (jdbc/db-do-commands db
                            (jdbc/create-table-ddl :subscribers
                                                   [[:id "varchar(36)" :primary :key]
                                                    [:name :text]
                                                    [:timestamp :datetime :default :current_timestamp]
                                                    [:report_request_id "varchar(36)"]
                                                    [:subscriber_site_id "varchar(36)"]] {:conditional? true}))
       (catch Exception e
         (println (.getMessage e)))))

(defn get-subscribers-by-report-request-id
  [db report-request-id]
  (jdbc/query db ["select * from subscribers where report_request_id = ?" report-request-id]))

(defn get-user-by-telegram-id
  [db telegram-id]
  (jdbc/query db ["select * from users where telegram_id = ?" telegram-id]))

(defn get-report-request-by-user-id-and-site
  [db user-id site site-user-id]
  (jdbc/query db ["select * from report_requests as rr left join users on rr.user_id = users.id where users.id = ? and rr.site = ? and site_user_id = ?" user-id site site-user-id]))

(defn get-report-request-by-id
  [db id]
  (let [report-request (first
                         (jdbc/query db ["select rr.id as id, rr.user_id as user_id, rr.site_user_id as site_user_id,
  users.telegram_id as telegram_id, users.name as user_name, rr.site as site
  from report_requests as rr left join users on rr.user_id = users.id where rr.id = ?" id]))]
    (assoc report-request :site (keyword (clojure.string/replace (report-request :site) #":" "")))))

(defn delete-subscribers [db subscriber-ids]
  (when (not-empty subscriber-ids)
    (let [ids (reduce #(str %1 "," %2) (map #(str "'" % "'") subscriber-ids))]
      (jdbc/execute! db [(str "DELETE FROM subscribers WHERE id in (" ids ")")]))))


(defn new-subscriber [report-request-id subscriber-site-id name]
  {:id (uuid) :name name :report_request_id report-request-id :subscriber_site_id subscriber-site-id})

(defn create-subscribers-for-request [db subscribers]
  (jdbc/insert-multi! db :subscribers subscribers))

(defn create-user [db name telegram-id]
  (jdbc/insert! db :users {:id (uuid) :name name :telegram_id telegram-id}))

(defn create-report-request [db user-id site-user-id site]
  (let [report-request-id (uuid)]
    (jdbc/insert! db :report_requests {:id report-request-id :user_id user-id :site_user_id site-user-id :site site})
    report-request-id))


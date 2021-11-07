(ns subscribers-bot.subscribers
  (:require
    [subscribers-bot.osnova]
    [subscribers-bot.data :as data]
    [subscribers-bot.osnova :as osnova]
    [morse.api :as t])
  (:import (org.apache.commons.lang StringEscapeUtils)))

(def debug false)

(defn not-contains? [collection key]
  (not (contains? collection key)))

(defn not-contains-in-current-subscriber-ids [current-subscribers-ids saved-subscriber]
  (not-contains? current-subscribers-ids (saved-subscriber :subscriber_site_id)))

(defn not-contains-in-saved-subscriber-ids [saved-subscriber-ids current-subscriber]
  (not-contains? saved-subscriber-ids (str (current-subscriber :id))))

(defn find-subscribed [current-subscribers saved-subscribers-site-ids]
  (filter #(not-contains-in-saved-subscriber-ids saved-subscribers-site-ids %) current-subscribers))

(defn find-unsubscribed [saved-subscribers current-subscribers-ids]
  (filter #(not-contains-in-current-subscriber-ids current-subscribers-ids %) saved-subscribers))

(defn current-subscribers-ids [current-subscribers]
  (into #{} (->> (map :id current-subscribers) (map str))))

(defn saved-subscribers-site-ids [saved-subscribers]
  (into #{} (map :subscriber_site_id saved-subscribers)))

(defn save-subscribers [db report-request-id current-subscribers]
  (when (not-empty current-subscribers)
    (data/create-subscribers-for-request db
                                         (map #(data/new-subscriber report-request-id (% :id) (% :name))
                                              current-subscribers))))

(defn build-user-url [base-url site-user-id]
  (str base-url "/u/" site-user-id))

(defn build-unsubscribed-html [unsubscribed base-url]
  (->> (map #(str "<a href=\"" (build-user-url base-url (% :subscriber_site_id)) "\">" (StringEscapeUtils/escapeHtml (% :name)) "</a>\n") unsubscribed)
       (reduce str)))

(defn send-report [db report-request-id tg-token]
  (let [report-request (data/get-report-request-by-id db report-request-id)
        report-request report-request
        saved-subscribers (data/get-subscribers-by-report-request-id db report-request-id)
        current-subscribers (osnova/get-subscribers (report-request :site) (report-request :site_user_id))
        {base-url :base_url name :name} (osnova/sites (report-request :site))
        unsubscribed (find-unsubscribed saved-subscribers (current-subscribers-ids current-subscribers))
        subscribed (find-subscribed current-subscribers (saved-subscribers-site-ids saved-subscribers))
        ]
    (if debug (do (println unsubscribed)
                  (println "____________________________________")
                  (println subscribed)
                  (println (build-unsubscribed-html unsubscribed base-url)))
              (do (data/delete-subscribers db (map :id unsubscribed))
                  (save-subscribers db (report-request :id) subscribed)))
    (if (empty? unsubscribed) (println "Unsubscribed users are not found")
                              (t/send-text tg-token (report-request :telegram_id) {"disable_web_page_preview" true "parse_mode" "HTML"}
                                           (str "<b>Unsubscribed on " name "</b>\n\n"
                                                (build-unsubscribed-html unsubscribed base-url))))
    )
  )

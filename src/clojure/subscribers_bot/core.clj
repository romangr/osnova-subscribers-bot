(ns subscribers-bot.core
  (:require [morse.handlers :as h]
            [morse.api :as t],
            [subscribers-bot.polling :as p],
            [subscribers-bot.data :as data],
            [subscribers-bot.osnova :as osnova]
            [subscribers-bot.schedule :as schedule]
            [subscribers-bot.subscribers :as subscribers]
            [subscribers-bot.telegram :as telegram])
  (:gen-class :main true))


(def welcome-message "Welcome to subscribers bot, send a link to your profile on TJ, DTF or VC to enable subscribers reports")

(defn start-handler [db {{id :id first_name :first_name} :chat}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (println "Bot joined new chat: " id)
  (t/send-text telegram/token id welcome-message))

(defn match-all-handler [scheduler db {{id :id first_name :first_name} :chat text :text}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (let [[site site-user-id] (osnova/parse-site-and-id text) user-id ((first (data/get-user-by-telegram-id db id)) :id)]
    (if (nil? site) (println (str "Unexpected message format: " text))
                    (if (empty? (data/get-report-request-by-user-id-and-site db user-id site))
                      (let [report-request-id (data/create-report-request db user-id site-user-id site)]
                        (t/send-text telegram/token id (str "You have added daily reports for " (name site)))
                        (schedule/schedule-job scheduler db report-request-id))
                      (t/send-text telegram/token id (str "You already enabled daily reports!"))))
    )
  )

(defn disable [scheduler db site telegram-id]
  (let [user (data/get-user-by-telegram-id db telegram-id)]
    (if (empty? user)
      (t/send-text telegram/token telegram-id "You are not registered, nothing to disable!")
      (let [report-request-list (data/get-report-request-by-user-id-and-site db (-> user first :id) site)
            report-request-id (if (empty? report-request-list) nil (-> report-request-list first :id))]
        (if (nil? report-request-id)
          (t/send-text telegram/token telegram-id "Nothing to disable!")
          (do (schedule/remove-job scheduler report-request-id)
              (data/delete-report-request-and-subscribers db report-request-id)
              (t/send-text telegram/token telegram-id "Subscribers report has been disabled")))
        ))))

(defn disable-tj [scheduler db {{telegram-id :id} :chat}]
  (disable scheduler db :tj telegram-id))

(defn disable-vc [scheduler db {{telegram-id :id} :chat}]
  (disable scheduler db :vc telegram-id))

(defn disable-dtf [scheduler db {{telegram-id :id} :chat}]
  (disable scheduler db :dtf telegram-id))


(defn -main []
  (let [db data/db
        scheduler (schedule/get-scheduler)
        bot-api (h/defhandler bot-api
                              ; Each bot has to handle /start and /help commands.
                              ; This could be done in form of a function:
                              (h/command-fn "start" #(start-handler db %))
                              (h/command-fn "help" #(start-handler db %))
                              (h/command-fn "disable_tj" #(disable-tj scheduler db %))
                              (h/command-fn "disable_dtf" #(disable-dtf scheduler db %))
                              (h/command-fn "disable_vc" #(disable-vc scheduler db %))
                              ;(h/command-fn "enable" enable-handler)

                              ; Handlers will be applied until there are any of those
                              ; returns non-nil result processing update.

                              ; Note that sending stuff to the user returns non-nil
                              ; response from Telegram API.

                              ; So match-all catch-through case would look something like this:
                              (h/message message (match-all-handler scheduler db message)))
        ]
    (data/create-users-table db)
    (data/create-report-requests-table db)
    (data/create-subscribers-table db)
    (p/start telegram/token bot-api)
    )
  )

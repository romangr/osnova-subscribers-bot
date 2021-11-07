(ns subscribers-bot.core
  (:require [morse.handlers :as h]
            [morse.api :as t],
            [subscribers-bot.polling :as p],
            [subscribers-bot.data :as data],
            [subscribers-bot.osnova :as osnova]
            [subscribers-bot.schedule :as schedule]
            [subscribers-bot.subscribers :as subscribers]
            [subscribers-bot.telegram :as telegram]
            ))

(def db data/db)
(data/create-users-table db)
(data/create-report-requests-table db)
(data/create-subscribers-table db)

(def welcome-message "Welcome to subscribers bot, send a link to your profile on TJ, DTF or VC to enable subscribers reports")

(defn start-handler [{{id :id first_name :first_name} :chat}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (println "Bot joined new chat: " id)
  (t/send-text telegram/token id welcome-message))

(defn match-all-handler [{{id :id first_name :first_name} :chat text :text}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (let [[site site-user-id] (osnova/parse-site-and-id text) user-id ((first (data/get-user-by-telegram-id db id)) :id)]
    (if (nil? site) (println (str "Unexpected message format: " text))
                    (if (empty? (data/get-report-request-by-user-id-and-site db user-id site site-user-id))
                      (let [report-request-id (data/create-report-request db user-id site-user-id site)]
                        (t/send-text telegram/token id (str "You have added daily reports for " (name site)))
                        (schedule/schedule-job schedule/scheduler db report-request-id))
                      (t/send-text telegram/token id (str "You already enabled daily reports!"))))
    )
  )

(h/defhandler bot-api
              ; Each bot has to handle /start and /help commands.
              ; This could be done in form of a function:
              (h/command-fn "start" start-handler)
              (h/command-fn "help" start-handler)
              ;(h/command-fn "enable" enable-handler)

              ; Handlers will be applied until there are any of those
              ; returns non-nil result processing update.

              ; Note that sending stuff to the user returns non-nil
              ; response from Telegram API.

              ; So match-all catch-through case would look something like this:
              (h/message message (match-all-handler message)))

(def channel (p/start telegram/token bot-api))



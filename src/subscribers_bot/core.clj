(ns subscribers-bot.core
  (:require [morse.handlers :as h]
            [morse.api :as t],
            [subscribers-bot.polling :as p],
            [subscribers-bot.data :as data],
            [subscribers-bot.osnova :as osnova]))

(def token (System/getenv "TELEGRAM_TOKEN"))
(def db-file-path (or (System/getenv "DB_FILE_PATH") "local.db"))

(def db (data/db db-file-path))
(data/create-users-table db)
(data/create-report-requests db)

(def welcome-message "Welcome to subscribers bot, send /enable to enable daily report about your subscribers")

(defn start-handler [{{id :id first_name :first_name} :chat}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (println "Bot joined new chat: " id)
  (t/send-text token id welcome-message))

;todo: replace with handling links from messages
(defn enable-handler [{{id :id first_name :first_name} :chat}]
  (if (empty? (data/get-user-by-telegram-id db id)) (data/create-user db first_name id))
  (t/send-text token id "You have enabled daily reports"))

(h/defhandler bot-api
              ; Each bot has to handle /start and /help commands.
              ; This could be done in form of a function:
              (h/command-fn "start" start-handler)
              (h/command-fn "help" start-handler)
              (h/command-fn "enable" enable-handler)

              ; Handlers will be applied until there are any of those
              ; returns non-nil result processing update.

              ; Note that sending stuff to the user returns non-nil
              ; response from Telegram API.

              ; So match-all catch-through case would look something like this:
              (h/message message (println "Intercepted message:" message)))

;(t/send-text token 48354307 "Hello, fellows")

;(def channel (p/start token bot-api))


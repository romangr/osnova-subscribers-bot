(ns subscribers-bot.schedule
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.conversion :as qc]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :as calendar-interval]
            [subscribers-bot.osnova]
            [subscribers-bot.subscribers :as subscribers]
            [subscribers-bot.osnova :as osnova]
            [subscribers-bot.telegram :as telegram]
            [subscribers-bot.data :as data]
            ))

(def db data/db)

(defjob update-subscribers-and-report
        [ctx]
        (let [m (qc/from-job-data ctx)
              report-request-id (m "request-id")
              ]
          (println (str "Running scheduled job for request id " report-request-id))
          (subscribers/send-report db report-request-id telegram/token)
          ))

;todo: move all subscribers filtration logic to "service" namespace

(defn job-key [request-id]
  (str "job." request-id))

(defn trigger-key [request-id]
  (str "trigger." request-id))

(def scheduler (-> (qs/initialize) qs/start))

(defn remove-job [scheduler request-id]
  (qs/delete-job scheduler (j/key (job-key request-id))))


(defn schedule-job
  [scheduler db request-id]
  (let [job (j/build
              (j/of-type update-subscribers-and-report)
              (j/using-job-data {"request-id" request-id})
              (j/request-recovery)
              (j/with-identity (j/key (job-key request-id))))
        trigger (t/build
                  (t/with-identity (t/key (trigger-key request-id)))
                  (t/start-now)
                  (t/with-schedule (calendar-interval/schedule
                                     (calendar-interval/with-interval-in-days 1))))]
    (qs/schedule scheduler job trigger)))

(defn -main []
  (remove-job scheduler "f7df45a4-ca75-482f-bdff-319a1def8663")
  (schedule-job scheduler db "f7df45a4-ca75-482f-bdff-319a1def8663")
  )

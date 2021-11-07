(ns subscribers-bot.osnova
  (:require [clj-http.client :as client]))

(def sites {
            :tj  {:base_url "https://tjournal" :api_base_url "https://api.tjournal.ru/v2.0/" :token "TBD!"}
            :dtf {:base_url "https://dtf.ru" :api_base_url "https://api.dtf.ru/v2.0/" :token "TBD!"}
            :vc  {:base_url "https://vc.ru" :api_base_url "https://api.vc.ru/v2.0/" :token "TBD!"}
            })

(defn resolve-site-by-domain [domain]
  ({"tjournal.ru" :tj "dtf.ru" :dtf "vc.ru" :vc} domain))

(defn get-subscribers
  "Retrieves all subscribers from site"
  [site user-id]
  (let [{api-base-url :api_base_url token :token} (sites site)]
    (client/get (concat api-base-url "subsite/subscribers"))
    {:headers      {:X-Device-Token token, :Content-Type "application/x-www-form-urlencoded"}
     :query-params {"subsiteId" user-id}}))
;todo: add pagination support
;subsite/subscribers?subsiteId=${site.user_id}${lastId ? "&lastId=" + lastId : ""}${lastSortingValue ? "&lastSortingValue=" + lastSortingValue : ""}

(defn parse-site-and-id [url]
  (let [[_ domain user-id] (re-matches #"https://(\w+\.ru)/u/(\d+)-.*" url)]
    [domain user-id]))

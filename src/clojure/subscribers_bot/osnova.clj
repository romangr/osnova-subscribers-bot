(ns subscribers-bot.osnova
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]))

(def sites {
            :tj  {:name "TJ" :base_url "https://tjournal.ru" :api_base_url "https://api.tjournal.ru/v2.0/" :token (System/getenv "TJ_TOKEN")}
            :dtf {:name "DTF" :base_url "https://dtf.ru" :api_base_url "https://api.dtf.ru/v2.0/" :token (System/getenv "DTF_TOKEN")}
            :vc  {:name "VC" :base_url "https://vc.ru" :api_base_url "https://api.vc.ru/v2.0/" :token (System/getenv "VC_TOKEN")}
            })

(defn resolve-site-by-domain [domain]
  ({"tjournal.ru" :tj "dtf.ru" :dtf "vc.ru" :vc} domain))

(defn- build-params
  [user-id last-id last-sorting-value]
  (cond-> {"subsiteId" user-id}
          (some? last-id) (assoc "lastId" last-id)
          (some? last-sorting-value) (assoc "lastSortingValue" last-sorting-value)))

(defn- execute-request [api-base-url token user-id last-id last-sorting-value]
  (-> (client/get (str api-base-url "subsite/subscribers")
                  {:headers      {"X-Device-Token" token "Content-Type" "application/x-www-form-urlencoded" "User-Agent" "Chrome/90.0.0.1"}
                   :query-params (build-params user-id last-id last-sorting-value)})
      :body
      (parse-string true)))

(defn- get-subscribers-internal
  [site user-id last-id last-sorting-value subscribers]
  (let [
        {api-base-url :api_base_url token :token} (sites site)
        response (execute-request api-base-url token user-id last-id last-sorting-value)
        items (-> response :result :items)
        new-last-id (-> response :result :lastId)
        new-last-sorting-value (-> response :result :lastSortingValue)]
    (if (empty? items) subscribers (recur site user-id new-last-id new-last-sorting-value (concat subscribers items)))
    ))

(defn get-subscribers
  "Retrieves all subscribers from site"
  [site user-id]
  (map (fn [{id :id name :name}] {:id id :name name}) (get-subscribers-internal site user-id nil nil [])))

(defn parse-site-and-id [url]
  (let [[_ domain user-id] (re-matches #"https://(\w+\.ru)/u/(\d+)-.*" url)]
    [(resolve-site-by-domain domain) user-id]))

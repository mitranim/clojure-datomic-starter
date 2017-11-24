(ns app.srv
  (:require
    [com.mitranim.forge :as forge :refer [sys]]
    [com.stuartsierra.component :as component]
    [org.httpkit.server :refer [run-server]]
    [hiccup.page :refer [html5]]
    [compojure.core :as compojure :refer [GET POST]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [clojure.walk :refer [keywordize-keys]]
    [ring.util.anti-forgery :refer [anti-forgery-field]]
    [app.util :as util :refer [getenv]]
    [app.dat :as dat])
  (:import
    [org.httpkit.server HttpServer]))

(set! *warn-on-reflection* true)



(def styles "
* {
  font-family: Menlo, Consolas, 'Helvetica Neue', Roboto, 'Segoe UI', Verdana, sans-serif;
  font-size: 16px;
}

body {
  padding: 1rem;
  max-width: 80ch;
}
")


(defn html-head [& content]
  [:head
   [:base {:href "/"}]
   [:meta {:charset "utf-8"}]
   [:link {:rel "icon" :href "data:;base64,="}]
   content])


(defn pp-str [value] (with-out-str (clojure.pprint/pprint value)))


(defn index-page [req]
  (let [dat (:dat sys)
        db @dat
        comments (when db (dat/q-comments db))]
    (html5
      (html-head
        [:style styles]
        [:title "Index"])
      [:body
       [:h3 "Datomic Starter"]
       [:p "Follow the setup instructions in the readme."]
       [:p "Database connection: "
        [:pre {:style "white-space: normal"} (:conn dat)]]
       (when (:conn dat)
         (list
           [:h3 "Create Comment"]
           [:form {:method "post" :action "/comment"}
            (anti-forgery-field)
            [:input {:name "comment/body"
                     :style "min-width: 50%; margin-right: 1rem; line-height: 1.4"}]
            [:button "Submit"]]))
       (when (:flash req)
         [:p "Message: " [:code (pp-str (:flash req))]])
       [:h3 "Comments"]
       (if-not (seq comments)
         [:p "No comments have been created yet"]
         (for [{:keys [inst comment/body]} (dat/q-comments db)]
           [:p [:span {:style "color: gray"} inst] [:span " "] [:span body]]))])))


(defn submit-comment [req]
  (let [body   (util/sanitize-permit-html (-> req :params :comment/body))
        result (dat/create-comment (:dat sys) body)]
    {:status 303
     :headers {"Location" "/"}
     :flash result}))



(def handler
  (->
    (compojure/routes
      (GET "/" [] index-page)
      (POST "/comment" [] submit-comment))
    util/wrap-keywordize-params
    (wrap-defaults site-defaults)
    forge/wrap-development-features))



(defrecord Srv [^HttpServer http-server]
  component/Lifecycle
  (start [this]
    (when http-server
      (.stop http-server 100))
    (let [port (Long/parseLong (getenv "LOCAL_PORT"))]
      (assoc this :http-server
        (-> (run-server handler {:port port}) meta :server))))
  (stop [this]
    (when http-server
      (.stop http-server 100))
    (assoc this :http-server nil)))

(defn new-srv [prev-sys]
  (new Srv nil))

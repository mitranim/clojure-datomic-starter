(ns app.util
  (:require
    [com.mitranim.forge :as forge]
    [autoclave.html :as ahtml]
    [clojure.walk]
    ))

(set! *warn-on-reflection* true)


(when-not (.exists (clojure.java.io/file ".env.properties"))
  (println "Env file not found."
           "Don't forget to copy/rename .env.properties.example to .env.properties"
           "and fill out your DB_URI"))


(def env (merge {} (System/getenv) (forge/read-props ".env.properties")))

(defmacro getenv [key] (forge/get-strict env key))



; Fix buggy implementation
(ns autoclave.html)
(defn- policy-factory [p] (if (instance? PolicyFactory p) p (policy p)))
(ns app.util)

(def permissive-html-policy
  (ahtml/merge-policies
    :BLOCKS
    :FORMATTING
    :IMAGES
    :LINKS
    :STYLES
    (ahtml/policy :allow-elements ["hr"])))

(defn sanitize-html [input]
  (when (string? input)
    (->> input
         (ahtml/sanitize permissive-html-policy)
         clojure.string/trim)))


(defn wrap-keywordize-params [handler]
  (comp handler #(update % :params clojure.walk/keywordize-keys)))

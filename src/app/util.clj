(ns app.util
  (:require
    [com.mitranim.forge :as forge]
    [clojure.string :as string]
    [clojure.walk])
  (:import
   [org.owasp.html HtmlPolicyBuilder PolicyFactory]))

(set! *warn-on-reflection* true)


(when-not (.exists (clojure.java.io/file ".env.properties"))
  (println "Env file not found."
           "Don't forget to copy/rename .env.properties.example to .env.properties"
           "and fill out your DB_URI"))


(def env (merge {} (System/getenv) (forge/read-props ".env.properties")))

(defmacro getenv [key] (forge/get-strict env key))



; These replacements seem to be hardcoded in OWASP.
; It seems to be geared towards sanitizing output, not input.
(defn unescape-common-text-symbols [value]
  (when (string? value)
    (-> value
        (string/replace "&amp;" "&")
        (string/replace "&#34;" "\"")
        (string/replace "&#39;" "'")
        (string/replace "&#43;" "+")
        (string/replace "&#61;" "=")
        (string/replace "&#64;" "@")
        (string/replace "&#96;" "`"))))

(def ^PolicyFactory policy-strip-html
  (-> (new HtmlPolicyBuilder)
      (.toFactory)))

(defn sanitize-strip-html [value]
  (when (string? value)
    (-> (.sanitize policy-strip-html value)
        (string/trim)
        (unescape-common-text-symbols))))

(def ^PolicyFactory policy-permit-html
  (-> (new HtmlPolicyBuilder)
      (.allowElements (into-array ["hr"]))
      (.allowCommonInlineFormattingElements)
      (.allowCommonBlockElements)
      (.allowStandardUrlProtocols)
      (.allowStyling)
      (.toFactory)))

(defn sanitize-permit-html [value]
  (when (string? value)
    (-> (.sanitize policy-permit-html value)
        (string/trim)
        (unescape-common-text-symbols))))



(defn wrap-keywordize-params [handler]
  (comp handler #(update % :params clojure.walk/keywordize-keys)))

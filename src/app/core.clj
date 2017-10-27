(ns app.core
  (:require
    [com.mitranim.forge :as forge]
    [com.stuartsierra.component :as component]
    [app.util :as util :refer [getenv]]
    [app.srv]
    [app.dat]
    ))

(set! *warn-on-reflection* true)

(defn create-system [prev-sys]
  (component/system-map
    :dat (app.dat/new-dat prev-sys (getenv "DB_URI"))
    :srv (app.srv/new-srv prev-sys)))

(defn main []
  (println "Starting system on thread" (str (Thread/currentThread)) "...")
  (forge/reset-system! create-system))

(defn main-dev []
  (forge/start-development! {:system-symbol 'app.core/create-system})
  (forge/reset)
  (println "Started server on" (str "http://localhost:" (getenv "LOCAL_PORT"))))

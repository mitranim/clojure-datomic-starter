(ns app.dat
  (:require
    [com.mitranim.forge :as forge]
    [com.stuartsierra.component :as component]
    [datomic.api :as d]
    [clojure.pprint :refer [pprint]]
    [app.util :as util]))

(set! *warn-on-reflection* true)


(defn try-create [db-uri]
  (try
    (when (d/create-database db-uri)
      (println "Created database at" db-uri))
    (catch Throwable err
      (binding [*out* *err*]
        (println "Couldn't create or reach database at" db-uri))
      (throw err))))

(defn try-connect [db-uri]
  (try (d/connect db-uri)
    (catch Throwable err
      (binding [*out* *err*]
        (println "Couldn't connect to database at" db-uri))
      (throw err))))


(defrecord Dat [db-uri conn]
  component/Lifecycle

  (start [this]
    (when-not conn (try-create db-uri))
    (let [conn (or conn (try-connect db-uri))
          this (assoc this :conn conn)]
      ; (when-not forge/development?
        (declare migrate)
        (migrate this)
        ; )
      this))

  (stop [this] this)

  clojure.lang.IDeref
  (deref [_] (when conn (d/db conn))))

(defmethod print-method Dat [dat out]
  (binding [*out* out] (clojure.pprint/simple-dispatch dat)))

(defmethod clojure.pprint/simple-dispatch Dat [dat]
  (print "#Dat")
  (pr (select-keys dat [:db-uri :conn])))



(defn new-dat [prev-sys db-uri] {:pre [(string? db-uri)]}
  (let [{prev-uri :db-uri conn :conn} (:dat prev-sys)]
    (if (= prev-uri db-uri)
      (new Dat db-uri conn)
      (new Dat db-uri nil))))



(def schema [
  {:db/ident       :comment/body
   :db/valueType   :db.type/string
   :db/cardinality :db.cardinality/one}
])


(defn migrate [^Dat dat]
  @(d/transact (:conn dat) schema)
  nil)



(def comments-q '
  [:find (pull ?e [:db/id :comment/body]) (min ?inst)
   :in $ $h
   :where
   [?e :comment/body]
   [$h ?e _ _ ?tx]
   [$h ?tx :db/txInstant ?inst]
   ])

(defn q-comments [db]
  (->>
    (for [[comment inst] (d/q comments-q db (d/history db))]
      (assoc comment :inst inst))
    (sort-by :inst)
    reverse))



(defn create-comment [^Dat dat body]
  (if-let [body (not-empty (util/sanitize-permit-html body))]
    (do
      @(d/transact (:conn dat) [{:comment/body body}])
      nil)
    {:error "Missing comment body"}))

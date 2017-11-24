(ns repl
  (:use clojure.repl)
  (:require
    [com.mitranim.forge :as forge :refer [sys]]
    [datomic.api :as d]
    [app.core :as core]
    [app.util :as util]))

(set! *warn-on-reflection* true)

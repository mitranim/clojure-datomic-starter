(defproject app "0.0.0"
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [com.datomic/datomic-pro "0.9.5561"]
    [com.stuartsierra/component "0.3.2"]
    [com.mitranim/forge "0.1.0"]
    [hiccup "1.0.5"]
    [compojure "1.6.0"]
    [ring/ring-defaults "0.3.1"]
    [alxlit/autoclave "0.2.0"]
  ]

  :main app.core

  :repl-options {:skip-default-init true
                 :init-ns repl
                 :init (app.core/-main-dev)}

  :profiles {:uberjar {:aot :all :omit-source true}}
)

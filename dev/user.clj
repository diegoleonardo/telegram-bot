(ns user
  (:require [integrant.core :as ig]
            [integrant.repl :as ig.repl]
            [work-permit-bot.core :as core]))

(ig.repl/set-prep! #(ig/prep core/config) #_(-> core/config
                     ig/prep
                     ig/init))

(defn go []
  (ig.repl/go))

(defn halt []
  (ig.repl/halt))

(defn reset []
  (ig.repl/reset))

(comment
  (go)
  (halt))

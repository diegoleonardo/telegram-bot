(ns work-permit-bot.core
  (:require [morse.handlers :as h]
            [morse.api :as t]
            [morse.polling :as p]
            [clojure.string :as str]
            [clojure.core.async :as async]
            [reader.spreadsheet :as sr])
  (:gen-class))

(def token "1223614094:AAEbGuXIVEu_OdpJME0LHISdjFX9lWzq4XA")

(def greetings ["olá" "olá!" "hola" "hola!" "hi" "hi!" "oi" "oi!" "oi, tudo bem?" "E aí" "hi there" "hi there!"])
(def positive-answer ["sim" "yes" "si" "ok" "claro" "yep" "positivo"])
(def negative-answer ["não" "no" "nop" "ñ" "não obrigado" "não, obrigado!" "negativo"])

(defn greetings? [text]
  (some #(= text %) greetings))

(defn positive-answer? [text]
  (some #(= text %) positive-answer))

(defn negative-answer? [text]
  (some #(= text %) negative-answer))

(def companies (atom nil))

(defn show-companies []
  (str "Ok. Essas são as empresas:\n" @companies))

(defn process-inc-message
  [message]
  (let [{:keys [chat]} message]
    (println "Intercepted message")
    (let [text (clojure.string/lower-case (:text message))
          id (:id chat)]
      (cond
        (greetings? text) (t/send-text token id "Olá, sou o bot de work-permit.\nVocê gostaria de ver as empresas que estão emitindo visto para permissão de trabalho na irlanda?")
        (positive-answer? text) (t/send-text token id (show-companies))
        (negative-answer? text) (t/send-text token id "Tudo bem. Se precisar, estou por aqui. Até a próxima!")
        :else (t/send-text token id "Desculpa, não entendi. Por gentileza, pode repetir?")))))

(h/defhandler bot-api
  (h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                          (println "Bot joined new chat: " chat)
                          (t/send-text token id "Olá, sou o work-permit bot. Muito prazer! Gostaria de ver quais empresas na irlanda estão emitindo permissão de trabalho?")))

  (h/command "help" {{id :id :as chat} :chat}
             (println "Help was requested in " chat)
             (t/send-text token id "Help is on the way"))

  (h/message-fn process-inc-message))

(def channel (p/start token bot-api))

(defn start-server
  []
  (p/start token bot-api))

(defn stop-server
  []
  (p/stop channel))

(defn -main
  [& args]
  (println "Initializing app")
  (sr/initialize)
  (reset! companies (sr/read-companies))
  (loop []
    (do (println "Doing things") (Thread/sleep 20000))
    (recur)))

#_(-main)

#_(stop-server)

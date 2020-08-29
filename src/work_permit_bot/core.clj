(ns work-permit-bot.core
  (:require [morse.handlers :as h]
            [morse.api :as t]
            [morse.polling :as p]
            [clojure.string :as str]
            [reader.spreadsheet :as sr]
            [integrant.core :as ig]
            [commons.utils :as util])
  (:gen-class))

(def token (util/getenv "TELEGRAM_API_TOKEN"))

(def greetings ["olá" "olá!" "hola" "hola!" "hi" "hello" "hi there" "hi!" "oi" "oi!" "oi, tudo bem?" "E aí" "hi there" "hi there!"])
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

(def config (ig/read-string (slurp "./resources/system.edn")))

(derive :bot/telegram :duct/daemon)

(defmethod ig/init-key :handler/foo [_ {}]
  bot-api)

(defmethod ig/prep-key :bot/telegram [_ opts]
  (sr/initialize)
  (reset! companies (sr/read-companies)))

(defmethod ig/init-key :bot/telegram [_ {:keys [bot-api]}]
  {:channel (p/start token bot-api)})

(defmethod ig/halt-key! :bot/telegram [_ {:keys [channel]}]
  (p/stop channel))

(defn -main
  [& args]
  (-> config
      ig/prep
      ig/init))

#_(-main)

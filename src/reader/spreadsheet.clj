(ns reader.spreadsheet
  (:require [dk.ative.docjure.spreadsheet :as spreadsheet]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [commons.utils :as util]))

(def data-source-url (util/getenv "TELEGRAM_BOT_DATA_SOURCE"))

(defn get-data!
  [url]
  (let [{:keys [body]} (client/get url {:as :byte-array})]
    body))

(defn read-spreadsheet
  [sheet filestream]
  (with-open [stream (clojure.java.io/input-stream filestream)]
    (->> (spreadsheet/load-workbook stream)
         (spreadsheet/select-sheet sheet)
         (spreadsheet/select-columns {:A :employername, :B :grandtotal}))))

(defn remove-unnecessary-data
  [f coll]
  (remove f coll))

(defn process-dbei-data
  []
  (->> (get-data! data-source-url)
       (read-spreadsheet "Sheet1")
       (remove-unnecessary-data #(or (nil? (:employername %)) (nil? (:grandtotal %))))))

(defn print-result [{:keys [employername grandtotal]}]
  (str employername " \n"))

(defn parse-spreedsheet []
  (let [body (get-data! data-source-url)]
   (with-open [stream (clojure.java.io/input-stream body)]
     (->> (spreadsheet/load-workbook stream)
          (spreadsheet/select-sheet "Sheet1")
          spreadsheet/row-seq
          (remove nil?)
          (map spreadsheet/cell-seq)
          (map #(map spreadsheet/read-cell %))
          (drop 4)
          (map #(first %))
          (vec)))))

(defn write-companies [companies]
  (with-open [w (clojure.java.io/writer "./resources/companies")]
    (doseq [company companies]
      (.write w company))))

(defn read-companies []
  (slurp "./resources/companies"))

(defn initialize []
  (->> (process-dbei-data)
       (map #(print-result %))
       (drop 1)
       (take 100)
       (vec)
       (write-companies)))

(comment
  (def dbei-data (process-dbei-data))

  dbei-data

  (def data (memoize get-data!))
  (def ireland-data (data data-source-url))

  (let [body (get-data! data-source-url)]
    (with-open [stream (clojure.java.io/input-stream body)]
      (->> (spreadsheet/load-workbook stream)
           (spreadsheet/select-sheet "Sheet1")
           spreadsheet/row-seq
           (remove nil?)
           (map spreadsheet/cell-seq)
           (map #(map spreadsheet/read-cell %))
           (drop 4)
           (map #(first %))
           (vec))
      ))

  )

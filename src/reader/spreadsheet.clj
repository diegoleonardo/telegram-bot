(ns reader.spreadsheet
  (:require [dk.ative.docjure.spreadsheet :as spreadsheet]
            [clj-http.client :as client]
            [clojure.java.io :as io]))

(def idbei-url "https://dbei.gov.ie/en/Publications/Publication-files/Permits-issued-to-Companies-2020.xlsx")

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
  (->> (get-data! idbei-url)
       (read-spreadsheet "Sheet1")
       (remove-unnecessary-data #(or (nil? (:employername %)) (nil? (:grandtotal %))))))

(defn print-result [{:keys [employername grandtotal]}]
  (str employername " \n"))

(defn parse-spreedsheet []
  (let [body (get-data! idbei-url)]
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
  (def ireland-data (data idbei-url))

  (let [body (get-data! idbei-url)]
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

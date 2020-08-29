(ns commons.utils)

(defn getenv
  ([env-name] (getenv env-name nil))
  ([env-name default-value]
   (or (System/getenv env-name) default-value)))

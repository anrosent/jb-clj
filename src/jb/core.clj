(ns jb.core
  (:require 
    [cheshire.core :as json]))

(defn get-type
  [v]
  (when (some? v)
    (-> v type .getName)))


(declare combine)

(defn combine-node
  [{t-in :type r-in :required} {t-new :type r-new :required}]
  (cond 
    (and (map? t-in) (map? t-new)) {:type (combine t-in t-new)
                                    :required (and r-in r-new)}
    (and (vector? t-in) (vector? t-new)) {:type (combine (first t-in) (first t-new))
                                          :required (and r-in r-new)}
    (or (set? t-in) (set? t-new)) {:type (clojure.set/union (if (set? t-in) t-in #{t-in})
                                                            (if (set? t-new) t-new #{t-new}))
                                   :required (and r-in r-new)}
    (= t-in t-new) {:type t-in
                    :required (and r-in r-new)}
    :else {:type #{t-in t-new}
           :required (and r-in r-new)}))

(defn combine
  [m1 m2]
  (reduce (fn [m k]
            (let [node-new (get m2 k)
                  node-in (get m k)]
              (cond 
                (and (some? node-in) (some? node-new)) (assoc m k (combine-node node-in node-new))
                ;; Only not required if m1 is nontrivial
                (some? node-new) (assoc m k (assoc node-new :required (empty? m1)))

                ;; Existing key isn't in new node, so not required
                (some? node-in)  (assoc m k (assoc node-in :required false)))))
          m1
          (set (concat (keys m1) (keys m2)))))

(defn browse
  [m]
  (reduce (fn [acc k]
            (let [v (acc k)]
              (cond
                (map? v)  (assoc acc k {:type (browse v)
                                        :required true})
                (coll? v) (assoc acc k {:type [(reduce combine {} (map browse v))]
                                        :required true})
                :else (assoc acc k {:type (-> (acc k) get-type)
                                    :required true}))))
          m
          (keys m)))


(defn -main
  [& args]
  (-> (json/parse-stream *in*)
      browse
      clojure.pprint/pprint))

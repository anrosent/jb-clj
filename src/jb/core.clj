(ns jb.core
  (:require 
    [cheshire.core :as json]))

(defn get-type
  "I: any
  O: representation of the Type of v"
  [v]
  (when (some? v)
    (-> v type .getName)))

;; Declare here so we can mutually recur
(declare combine)

(defn combine-node
  "Merges two schemas for the same property"
  [{t-in :type r-in :required} {t-new :type r-new :required}]
  (let [r (and r-in r-new)
        t (cond 
            (and (map? t-in) 
                 (map? t-new))    (combine t-in t-new)
            (and (vector? t-in) 
                 (vector? t-new)) (combine (first t-in) (first t-new))
            (or (set? t-in) 
                (set? t-new))     (clojure.set/union (if (set? t-in) t-in #{t-in})
                                                     (if (set? t-new) t-new #{t-new}))
            (= t-in t-new)        t-in
            :else                 #{t-in t-new})]
    {:type t :required r}))

(defn combine
  "Merges two Schemas for the same object"
  [m1 m2]
  (reduce (fn [m k]
            (let [node-new (get m2 k)
                  node-in (get m k)
                  combined (cond 

                             ;; Combine if both nontrivial 
                             (and (some? node-in) (some? node-new)) (combine-node node-in node-new)

                             ;; Only not required if m1 doesn't have key *and* is nontrivial
                             (some? node-new)  (assoc node-new :required (empty? m1))

                             ;; Existing key isn't in new node, so not required
                             (some? node-in)  (assoc node-in :required false)

                             ;; this can't happen!!
                             :else (throw (Exception. "Unreachable code has been reached!")))]
              (assoc m k combined)))
          m1
          (set (concat (keys m1) (keys m2)))))

(defn infer
  "Runs schema-inference on the parsed JSON object
  I: map
  O: a Schema descriptor map"
  [m]
  (reduce (fn [acc k]
            (let [v (acc k)
                  t (cond
                      (map? v)  (infer v)
                      (coll? v) [(reduce combine {} (map infer v))]
                      :else     (->> acc k get-type))]
              (assoc acc k {:type t :required true})))
          m
          (keys m)))

(defn browse 
  "Pretty-prints the schema"
  [schema]
  (clojure.pprint/pprint schema))

(defn -main
  [& args]
  (-> (json/parse-stream *in*)
      infer
      browse))

(ns jb.browse
  (:require
    [cheshire.core :as json]
    [jb.core :refer [infer-schema] :as core]))

(defn map-vals [f m] (zipmap (keys m) (map f (vals m))))

(declare simplify)

(defn- simplify-type [{id ::core/id schema ::core/schema}] 
  (if (= id ::core/type.id.maybe) 
    {:maybe (simplify schema)}
    (simplify schema)))

(defmulti simplify ::core/id)
(defmethod simplify ::core/schema.id.primitive [{n ::core/schema.data.primitive}] n)
(defmethod simplify ::core/schema.id.listof [{t ::core/schema.data.listof}] [(simplify-type t)]) ;;cheating a bit
(defmethod simplify ::core/schema.id.object [{m ::core/schema.data.object}] (map-vals simplify-type m))
(defmethod simplify ::core/schema.id.union [{m ::core/schema.data.union}] {:union (map simplify m)})

(defn render
  [schema]
  (json/generate-string (simplify schema) {:pretty true}))

(defn browse 
  "Pretty-prints the schema as JSON"
  [data]
  (-> data
      core/infer-schema
      render))

(defn -main
  [& args]
  (-> (json/parse-stream *in*)
            browse
            println))

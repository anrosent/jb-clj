(ns jb.core
  (:require 
    [clojure.spec.alpha :as s]
    [cheshire.core :as json]))

(s/def ::name string?)
(s/def ::value string?)
(s/def ::id keyword?)

(defmulti  type-id ::id)
(defmethod type-id ::type.id.always [_] (s/keys :req [::id ::schema]))
(defmethod type-id ::type.id.maybe [_] (s/keys :req [::id ::schema]))
(s/def ::type (s/multi-spec type-id ::id))

(defmulti  schema-id ::id)
(defmethod schema-id ::schema.id.union [_] (s/keys :req [::id ::schema.data.union]))
(defmethod schema-id ::schema.id.primitive [_] (s/keys :req [::id ::schema.data.primitive]))
(defmethod schema-id ::schema.id.object [_] (s/keys :req [::id ::schema.data.object]))
(defmethod schema-id ::schema.id.listof [_] (s/keys :req [::id ::schema.data.listof]))
(s/def ::schema (s/multi-spec schema-id ::id))
(s/def ::schema.data.union (s/coll-of ::schema))
(s/def ::schema.data.object (s/map-of string? ::type))
(s/def ::schema.data.listof ::type)
(s/def ::schema.data.primitive ::name)

(s/fdef get-name
        :args (s/cat :v ::value)
        :ret ::name)
(s/fdef combine-types
        :args (s/cat :type-left ::type :type-right ::type)
        :ret ::type)
(s/fdef combine-schemas
        :args (s/cat :schema-left ::schema :schema-right ::schema)
        :ret ::schema)
(s/fdef infer-schema
        :args (s/cat :m ::json)
        :ret ::schema)
(s/fdef browse
        :args (s/cat :schema ::schema)
        :ret string?)

(defn get-name
  "I: any
  O: representation of the Type of v"
  [v]
  (when (some? v)
    (-> v type .getName)))

(declare combine-types combine-schemas infer-schema infer-type simplify)

(defn make-union
  [{id-left ::id :as schema-left} {id-right ::id :as schema-right}]
  (cond 
    (and (= id-left ::schema.id.union)
         (= id-right ::schema.id.union)) (clojure.set/union (::schema.data.union schema-left)
                                                            (::schema.data.union schema-right))
    (= id-left ::schema.id.union)        (conj (::schema.data.union schema-left) schema-right)
    (= id-right ::schema.id.union)       (conj (::schema.data.union schema-right) schema-left)
    :else                                #{schema-left schema-right}))

(defn combine-schemas
  "Merges two schemas"
  [{id-left ::id :as schema-left} {id-right ::id :as schema-right}]
  (if (not= id-left id-right)
    {::id ::schema.id.union
     ::schema.data.union (make-union schema-left schema-right)}
    (case id-left
      ::schema.id.object (let [object-left  (::schema.data.object schema-left)
                               object-right (::schema.data.object schema-right)
                               allkeys (set (concat (keys object-left) (keys object-right)))]
                           {::id ::schema.id.object
                            ::schema.data.object (zipmap allkeys (map (comp (partial apply combine-types) 
                                                                            (juxt object-left object-right))
                                                                      allkeys))})
      ::schema.id.listof (let [type-left (::schema.data.listof schema-left)
                               type-right (::schema.data.listof schema-right)]
                           {::id ::schema.id.listof
                            ::schema.data.listof (combine-types type-left type-right)})
      ::schema.id.primitive (let [name-left (::schema.data.primitive schema-left)
                                  name-right (::schema.data.primitive schema-right)]
                              (if (= name-left name-right)
                                {::id ::schema.id.primitive
                                 ::schema.data.primitive name-left}
                                {::id ::schema.id.union
                                 ::schema.data.union (make-union schema-left schema-right)})))))

(defn combine-types 
  [{id-left ::id schema-left ::schema :as type-left} {id-right ::id schema-right ::schema :as type-right}]
  (cond 
    (nil? type-left) (assoc type-right ::id ::type.id.maybe)
    (nil? type-right) (assoc type-left ::id ::type.id.maybe)
    :else (let [schema (cond 
                         (= schema-left schema-right) schema-left
                         :else (combine-schemas schema-left schema-right))
                id (cond
                     (= id-left id-right) id-left
                     :else ::type.id.maybe)]
            {::id id ::schema schema})))

(defn infer-type
  "Runs inference on the parsed JSON object
  I: any
  O: a Schema descriptor map"
  [data]
  {::id ::type.id.always
   ::schema (infer-schema data)})

(defn infer-schema
  [data]
  (cond
    (map? data) {::id ::schema.id.object
                 ::schema.data.object (zipmap (keys data) (map infer-type (vals data)))}
    (coll? data) {::id ::schema.id.listof
                  ::schema.data.listof (reduce combine-types (map infer-type data))}
    :else        {::id ::schema.id.primitive
                  ::schema.data.primitive (get-name data)}))

(defn map-vals [f m] (zipmap (keys m) (map f (vals m))))

(defn- simplify-type [{id ::id schema ::schema}] 
  (if (= id ::type.id.maybe) 
    {:maybe (simplify schema)}
    (simplify schema)))

(defmulti simplify ::id)
(defmethod simplify ::schema.id.primitive [{n ::schema.data.primitive}] n)
(defmethod simplify ::schema.id.listof [{t ::schema.data.listof}] [(simplify-type t)]) ;;cheating a bit
(defmethod simplify ::schema.id.object [{m ::schema.data.object}] (map-vals simplify-type m))
(defmethod simplify ::schema.id.union [{m ::schema.data.union}] {:union (map simplify m)})


(defn render
  [schema]
  (json/generate-string (simplify schema) {:pretty true}))

(defn browse 
  "Pretty-prints the schema as JSON"
  [data]
  (-> data
      infer-schema
      render))

(defn -main
  [& args]
  (time (-> (json/parse-stream *in*)
            browse
            println)))

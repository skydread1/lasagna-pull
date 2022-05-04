(ns robertluo.pullable.schema
  (:require
   [malli.core :as m]
   [malli.transform :as mt]))

(def pattern-schema
  [:schema
   {:registry
    {::wild-var     [:fn '(fn [v] (= v '?))]
     ::named-var    [:and :symbol [:fn '(fn [v] (re-matches #"\?.+" (name v)))]]
     ::logical-var  [:orn [:wild ::wild-var] [:named ::named-var]]
     ::option       [:cat :keyword :any]
     ::qk           [:orn [:k [:not sequential?]] [:list [:cat :any [:* ::option]]]]
     ::qv           [:orn [:var ::logical-var] [:joining [:ref ::pattern]]]
     ::vec          [:map-of ::qk [:ref ::qv]]
     ::seq          [:cat ::vec [:? ::logical-var] [:* ::option]]
     ::pattern      [:or [:ref ::vec] [:ref ::seq]]}}
   ::pattern])

(def query-parser (m/parser pattern-schema))

(defn xformer []
  (mt/transformer
   {:decoders {:map-of #(update % :a str)}}))

(def decorder (m/decoder pattern-schema xformer))

(comment
  (update '{:a ?} :a str)
  (m/decode pattern-schema '{:a ?} xformer)
  (decorder '{:a ?})
  )

(def query-decoder (m/decoder pattern-schema
                              (mt/transformer
                               {:name :query})))

(comment
  (def ptn '{:a [{(:b :not-found ::ok) ? :c ? :d {:e ?}} ?x :seq [1 3]]})
  (query-parser ptn)
  (query-decoder ptn)
  )

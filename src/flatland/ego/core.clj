(ns flatland.ego.core
  (:require [clojure.string :as s]
            [flatland.useful.fn :refer [fix]]
            [flatland.useful.utils :refer [verify]]))

(defn split-id*
  "Split an id on dash, returning the type as a string followed by the identifier."
  [^String id]
  (let [parts (s/split id #"-" 2)]
    (if (= 1 (count parts))
      [nil (first parts)]
      parts)))

(defn split-id
  "Split an id on dash. Optionally pass a function (such as a set) that will be passed
   the id's type. If this function returns false, an error will be thrown."
  [^String id & [expected]]
  (let [[type ident] (split-id* id)
        type (when type
               (keyword type))]
    (verify (or (nil? expected) (expected type))
            (if (set? expected)
              (format "node-id %s doesn't match type(s): %s"
                      id (s/join ", " (map name expected)))
              "node-id's type was not what was expected"))
    [type ident]))

(defn id-number
  "Split the provided id and convert to a Long. Optionally, pass a function to validate the id."
  [id & [expected]]
  (if (string? id)
    (Long. ^String (last (split-id id expected)))
    id))

(defn make-id*
  [type id]
  (str (name type) "-" id))

(defn make-id
  [type id]
  (make-id* type (fix id string? (comp last split-id))))

(defn type-key
  "Given an id or type, return its type as a keyword."
  [type-or-id]
  (if (keyword? type-or-id)
    type-or-id
    (first (split-id type-or-id))))

(defn type-name
  "Given an id or type, return its type as a string."
  [type-or-id]
  (if (keyword? type-or-id)
    (name type-or-id)
    (first (split-id* type-or-id))))

(defn type?
  "Check to see if the type of id matches on of the given set of type keywords."
  [type ^String id]
  (if (set? type)
    (contains? type (type-key id))
    (let [type-str ^String (name type)
          type-len (.length type-str)
          id-len (.length id)]
      (and (> id-len type-len)
           (.startsWith id type-str)
           (= \- (.charAt id type-len))))))

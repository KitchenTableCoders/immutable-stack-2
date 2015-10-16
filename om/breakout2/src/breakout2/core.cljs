(ns breakout2.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

(enable-console-print!)

(def init-data
  {:address-book
   [{:name "Bob Smith"
     :email "bob.smith@gmail.com"}
    {:name "Laura Grant"
     :phone-number "111-1111"}
    {:name "Rita Black"
     :email "rita.black@hotmail.com"
     :phone-number "111-1112"}]
   :friends
   [{:name "Bob Smith" :age 35}
    {:name "Rita Black" :age 34}]})

(defmulti read om/dispatch)

(defmethod read :address-book
  [{:keys [state]} key params]
  (let [st @state]
    {:value (into [] (map #(get-in st %)) (get st key))}))

(defmethod read :friends
  [{:keys [state]} key params]
  (let [st @state]
    {:value (into [] (map #(get-in st %)) (get st key))}))

(defui AddressBookEntry
  static om/Ident
  (ident [this {:keys [name]}]
    [:person/by-name name])
  static om/IQuery
  (query [this]
    [:name :email :phone-number]))

(defui Friend
  static om/Ident
  (ident [this {:keys [name]}]
    [:person/by-name name])
  static om/IQuery
  (query [this]
    [:name :age]))

(defui MyApp
  static om/IQuery
  (query [this]
    [{:friends (om/get-query Friend)}
     {:address-book (om/get-query AddressBookEntry)}]))

(comment
  ;; Exercise 1: Check the MyApp query
  (om/get-query MyApp)
  ;; Exercise 2: normalize the data by running the following
  ;;   normalize uses the query on the first argument to
  ;;   transform the second argument
  (pprint/pprint (om/normalize MyApp init-data))
  ;; Exercise 3: normalize the data by running the following
  ;;   What's the difference?
  (pprint/pprint (om/normalize MyApp init-data true))
  ;; Exercise 4: put the normalized data into an atom and
  ;;   store the atom in a var called "norm"
  ;; Exercise 5: make a parser using the supplied "read" function
  ;;   put this in var called "parser"
  ;; Exercise 6: parse the data back into a tree with the following
  ;;   expression. What's interesting about the result?
  )
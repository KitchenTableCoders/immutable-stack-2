(ns breakout2.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Part 1

(defn read1 [env key params]
  (let [st    @(:state env)
        value (get st key)]
    (cond
      (= :not/here! key) {:remote true}

      value {:value value}

      :else
      {:value :not-found})))

(def my-state (atom {:foo 1 :bar 2}))
(def parser1 (om/parser {:read read1}))

(comment
  ;; Exercise 1: run the following expressions
  (parser1 {:state my-state} [:foo])
  (parser1 {:state my-state} [:foo :bar])
  (parser1 {:state my-state} [:foo :bar :not/here!])
  ;; Exercise 2: Why did :not/here! appear in the previous one? Try the
  ;;   following
  (parser1 {:state my-state} [:foo :bar :not/here!] {:remote true})
  )

;; -----------------------------------------------------------------------------
;; Part 2

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

(defmulti read2 om/dispatch)

(defmethod read2 :address-book
  [{:keys [state]} key params]
  (let [st @state]
    {:value (into [] (map #(get-in st %)) (get st key))}))

(defmethod read2 :friends
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
  ;; Exercise 5: make a parser using the supplied "read2" function
  ;;   put this in var called "parser2"
  ;; Exercise 6: parse the data back into a tree with the following
  ;;   expression. What's interesting about the result?
  )
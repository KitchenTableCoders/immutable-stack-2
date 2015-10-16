(ns breakout1.core
    (:require
      [datomic.api :as d]
      [clojure.java.io :as io]
      [clojure.edn :as edn]))

(comment


  ;; Set up database

  (def uri "datomic:mem://localhost:4334/contacts")

  (d/create-database uri)

  (def conn (d/connect uri))

  (d/transact
    conn
    (read-string (slurp (io/resource "data/schema.edn"))))

  (map
    #(d/transact conn %)
    (read-string
      (slurp (io/resource "data/initial.edn"))))

  ;; Datalog queries


  ;; find all last names
  (d/q '[:find ?ln
         :where
         [_ :person/last-name ?ln]]
       (d/db conn))


  ;; find first and last names
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]]
       (d/db conn))


  ;; find entity and last names for specific first name value
  (d/q '[:find ?e ?ln
         :where
         [?e :person/first-name "Kovas"]
         [?e :person/last-name ?ln]]
       (d/db conn))


  ;; same, but for specific last name value
  (d/q '[:find ?e ?fn
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name "Boguta"]]
       (d/db conn))


  ;; what happens when we don't force variables to bind to same entity?
  (d/q '[:find ?fn ?ln
         :where
         [_ :person/first-name ?fn]
         [_ :person/last-name ?ln]]
       (d/db conn))


  ;; using Clojure functions to compute variable values
  (d/q '[:find ?name
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [(str ?fn " " ?ln) ?name]]
       (d/db conn))


  ;; negation
  (d/q '[:find ?e ?fn
         :where
         [?e :person/first-name ?fn]
         (not [?e :person/last-name "Smith"])]
       (d/db conn))

  (d/q '[:find ?e ?fn
         :where
         [?e :person/first-name ?fn]
         (not (not [?e :person/last-name "Smith"]))]
       (d/db conn))


  ;; who has a telephone number?
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [?e :person/telephone _]]
       (d/db conn))

  ;; who has an address?
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [?e :person/address _]]
       (d/db conn))

  ;; who has both a telephone and address?
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [?e :person/telephone _]
         [?e :person/address _]]
       (d/db conn))

  ;; who has either a telephone or address?
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         (or
           [?e :person/telephone _]
           [?e :person/address _])]
       (d/db conn))

  ;; who has neither?
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         (not
           (or
             [?e :person/telephone _]
             [?e :person/address _]))]
       (d/db conn))

  ;; querying relations across multiple entities
  ;; What city are people in?
  (d/q '[:find ?fn ?ln ?c
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [?e :person/address ?a]
         [?a :address/city ?c]]
       (d/db conn))

  ;; People in Brooklyn
  (d/q '[:find ?fn ?ln
         :where
         [?e :person/first-name ?fn]
         [?e :person/last-name ?ln]
         [?e :person/address ?a]
         [?a :address/city "Brooklyn"]]
       (d/db conn))

  ;; All cities
  (d/q '[:find ?c
         :where
         [?a :address/city ?c]]
       (d/db conn))


  ;; Emails for people in brooklyn
  (d/q '[:find ?ea
         :where
         [?a :address/city "Brooklyn"]
         [?p :person/address ?a]
         [?p :person/email ?e]
         [?e :email/address  ?ea]]
       (d/db conn))

  ;; Many-to-Many relations
  ;; Find all members of the organizations David Nolen belongs to
  (d/q '[:find ?on ?fn ?ln
         :where
         [?p :person/first-name "David"]
         [?p :person/last-name "Nolen"]
         [?p :person/org ?o]
         [?o :org/name ?on]
         [?p2 :person/org ?o]
         [?p2 :person/first-name ?fn]
         [?p2 :person/last-name ?ln]]
       (d/db conn))



  ;; Parameterizing the query

  ;; Find people with given last name
  (d/q '[:find ?e
         :in $ ?ln
         :where
         [?e :person/last-name ?ln]]
       (d/db conn) "Boguta")

  ;; Find people with a set of last names
  (d/q '[:find ?fn ?ln
         :in $ [?ln ...]
         :where
         [?e :person/last-name ?ln]
         [?e :person/first-name ?fn]]
       (d/db conn) ["Boguta" "Nolen"])

  ;; Pluck variables out of a Clojure map
  (d/q '[:find ?day ?fn ?ln
         :in $ [[?day ?ln]]
         :where
         [?e :person/last-name ?ln]
         [?e :person/first-name ?fn]]
       (d/db conn) {:day1 "Boguta" :day2 "Nolen"})


  ;; The Pull API

  (def kovas-entity
    (ffirst
      (d/q '[:find ?e
             :where
             [?e :person/last-name "Boguta"]]
           (d/db conn))))

  (d/pull
    (d/db conn)
    '[*]
    kovas-entity)

  (d/pull
    (d/db conn)
    [:person/first-name]
    kovas-entity)

  (d/pull
    (d/db conn)
    [:person/first-name :person/last-name]
    kovas-entity)

  (d/pull
    (d/db conn)
    [:person/first-name :person/last-name :person/address]
    kovas-entity)

  (d/pull
    (d/db conn)
    [:person/first-name :person/last-name {:person/address [:address/city]}]
    kovas-entity)

  ;; Using pull inside of query
  (d/q '[:find (pull ?e [*])
         :where
         [?e :person/address ?a]
         [?a :address/city "Brooklyn"]]
       (d/db conn))

  ;; supply pull specification as a parameter
  (d/q '[:find (pull ?e patt)
         :in $ patt
         :where
         [?e :person/address ?a]
         [?a :address/city "Brooklyn"]]
       (d/db conn)
       '[*])


  ;; Challenges

  ;; 1. Find all organizations

  ;; 2. Find all people who are members of any organization, return first name, last name, and organization name

  ;; 3. Write function that takes organization name, and returns member first and last names

  ;; 4. Write function that finds same people as in 3, but accepts a pull selector as an argument. Pull just the first name.

  ;; 5. Find organizations with members in Brooklyn

  ;; 6. Find organizations with no members in Boston


  )









































(comment

  ;; 1. Find all organizations and their names
  (d/q
    '[:find ?o ?on
      :where
      [?o :org/name ?on]]
    (d/db conn))

  ;; 2. Find all people who are members of any organization, return first name, last name, and organization name
  (d/q
    '[:find ?fn ?ln ?on
      :where
      [?o :org/name ?on]
      [?p :person/org ?o]
      [?p :person/first-name ?fn]
      [?p :person/last-name ?ln]]
    (d/db conn))

  ;; 3. Write a function that takes the organization name, and returns the names of its members
  (defn challenge3 [name]
    (d/q
      '[:find ?fn ?ln
        :in $ ?on
        :where
        [?o :org/name ?on]
        [?p :person/org ?o]
        [?p :person/first-name ?fn]
        [?p :person/last-name ?ln]]
      (d/db conn)
      name))

  (challenge3 "Cognitect")



  ;; 4. Write function that finds same people as in 3, but accepts a pull selector as the 2nd argument. Pull just the first name.
  (defn challenge4 [name selector]
    (d/q
      '[:find (pull ?p s)
        :in $ ?on s
        :where
        [?o :org/name ?on]
        [?p :person/org ?o]
        [?p :person/first-name ?fn]
        [?p :person/last-name ?ln]]
      (d/db conn)
      name
      selector))

  (challenge4 "Cognitect" [:person/first-name])


  ;; 5. Find organizations with members in Brooklyn
  (d/q
    '[:find ?o
      :where
      [?a :address/city "Brooklyn"]
      [?p :person/address ?a]
      [?p :person/org ?o]]
    (d/db conn))

  ;; 6. Find organizations with no members in Boston
  (d/q
    '[:find ?o
      :where
      (not [?a :address/city "Boston"])
      [?p :person/address ?a]
      [?p :person/org ?o]]
    (d/db conn))




  )







(d/q '[:find ?ln
       :where
       [_ :org/name ?ln]]
     (d/db conn))


(d/q '[:find ?e ?ident
       :where
       [?e :person/first-name ?fn]
       [?e ?attr ?val]
       [?attr :db/ident ?ident]]
     (d/db conn))

(d/q '[:find ?ident
       :where
       [?e :person/first-name ?fn]
       [?e ?attr ?val]
       [?attr :db/ident ?ident]]
     (d/db conn))
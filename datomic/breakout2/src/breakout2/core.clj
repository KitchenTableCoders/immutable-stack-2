(ns breakout2.core
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


  ;; query we will be reusing
  (defn full-names [db]
    (d/q
      '[:find ?e ?fn ?ln
        :where
        [?e :person/first-name ?fn]
        [?e :person/last-name ?ln]]
      db))

  ;; transact to an existing entity

  (def db1 (d/db conn))

  (full-names db1)

  (def eid (ffirst
             (d/q '[:find ?e
                    :in $ ?ln
                    :where
                    [?e :person/last-name ?ln]]
                  db1 "Boguta")))

  eid


  (d/transact
    conn
    [[:db/add eid :person/first-name "World Peace"]])

  (def db2 (d/db conn))

  (full-names db2)

  (full-names db1)

  ;; transact to new entity

  (let [id (d/tempid :db.part/user)]
    (d/transact
      conn
      [[:db/add id :person/first-name "Alan"]
       [:db/add id :person/last-name "Turing"]]))

  (full-names (d/db conn))


  ;; map-based syntax

  (let [id (d/tempid :db.part/user)]
    (d/transact
      conn
      [{:db/id id
        :person/first-name "Alonzo"
        :person/last-name "Church"}]))

  (full-names (d/db conn))

  ;; resolving tempids

  (def tid (d/tempid :db.part/user))

  (def transaction-result
    (d/transact
      conn
      [{:db/id tid
        :person/first-name "Alan"
        :person/last-name "Kay"}]))


  (def real-id
    (d/resolve-tempid (d/db conn)
                     (:tempids @transaction-result)
                     tid))

  (d/pull (d/db conn) '[*] real-id)

  ;; tempid literal

  (d/transact
    conn
    [{:db/id             #db/id[:db.part/user]
      :person/first-name "John"
      :person/last-name  "von Neumann"}])


  (full-names (d/db conn))

  ;; transacting multiple new entities

  (let [person-id (d/tempid :db.part/user)
        email-id (d/tempid :db.part/user)]
    (d/transact
      conn
      [{:db/id             person-id
        :person/first-name "Emil"
        :person/last-name  "Post"
        :person/email      [{:db/id         email-id
                             :email/address "emil@email.com"}]}]))

  (d/q
    '[:find ?email
      :where
      [?e :person/first-name "Emil"]
      [?e :person/last-name "Post"]
      [?e :person/email ?email-account]
      [?email-account :email/address ?email]]
    (d/db conn))

  ;; transacting a schema

  (d/transact
    conn
    [{:db/id #db/id[:db.part/db]
      :db/ident :person/birthplace
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db/doc "A person's birthplace"
      ;; This last part installs the schema!
      :db.install/_attribute :db.part/db}])


  ;; find all attributes with string type
  (d/q
    '[:find ?i
      :where
      [?a :db/valueType :db.type/string]
      [?a :db/ident ?i]]
    (d/db conn))


  ;; transaction functions

  ;; fails
  (d/transact
    conn
    [[:db.fn/cas eid :person/first-name "Kovas" "Cthulhu"]])

  (full-names (d/db conn))

  ;; succeed
  (d/transact
    conn
    [[:db.fn/cas eid :person/first-name "World Peace" "Cthulhu"]])

  (full-names (d/db conn))


  ;; History

  (def current-db (d/db conn))

  (def current-t (d/basis-t current-db))

  current-t

  (full-names (d/as-of current-db (- current-t 20)))

  (full-names (d/since current-db (- current-t 20)))

  (full-names (d/as-of
                (d/since
                  current-db
                  (- current-t 20))
                (- current-t 5)))

  (d/q '[:find ?ln ?timestamp ?tx
         :where
         [_ :person/last-name ?ln ?tx]
         [?tx :db/txInstant ?timestamp]]
       current-db)

  (d/pull current-db '[*] 13194139534316)

  ;; get the basis-t for a transaction id
  (d/tx->t 13194139534316)

  ;; annotating transactions

  (def result
    (d/transact
     conn
     [{:db/id (d/tempid :db.part/user) :org/name "Immutable Stack 2"}
      {:db/id (d/tempid :db.part/tx) :db/doc "Seems like a good idea"}]))

  
  (d/t->tx
    (d/basis-t (:db-after @result)))


  (let [db (:db-after @result)]
    (d/pull
     db
     '[*]
     (d/t->tx
       (d/basis-t db))))



  ;; Challenges
  ;; 1. Transact yourself into the db. Verify all the data is there.
  ;; Either use the resolve tempids + pull method, or find using query

  ;; 2. Transact a schema for an :person/age attribute. Add data for yourself

  ;; 3. Find the transaction id corresponding for #1

  ;; 4. Find entities with a :db/ident attribute that has changed after the transaction found in #3




























  ;; Challenges
  ;; 1. Transact yourself into the db. Verify all the data is there.
  (def my-id (d/tempid :db.part/user))

  (def transaction-result
    (d/transact
     conn
     [{:db/id             my-id
       :person/first-name "Immutable"
       :person/last-name  "Learner"
       :person/telephone  [{:telephone/number "111-111-1119"}]
       :person/address    [{:address/street  "274 Morgan Ave"
                            :address/city    "Brooklyn"
                            :address/state   "New York"
                            :address/zipcode "11211"}]}]))

  (def real-id
    (d/resolve-tempid (d/db conn)
                      (:tempids @transaction-result)
                      my-id))

  (d/pull (d/db conn) '[*] real-id)

  ;; 2. Transact a schema for an :person/age attribute. Add data for yourself
  (d/transact
    conn
    [{:db/id                 #db/id[:db.part/db]
      :db/ident              :person/age
      :db/valueType          :db.type/long
      :db/cardinality        :db.cardinality/one
      :db/isComponent        true
      :db/doc                "A person's age"
      :db.install/_attribute :db.part/db}]
    )

  (d/transact conn [{:db/id real-id :person/age 35}])

  (d/pull (d/db conn) '[:person/age] real-id)


  ;; 3. Find the transaction id corresponding for #1
  (d/q '[:find  ?tx
         :where
         [_ :person/last-name "Learner" ?tx]]
       (d/db conn))

  ;; 4. Find values for that :db/ident attribute that have changed after the transaction found in #3
  (d/q
    '[:find ?i
      :where
      [?e :db/ident ?i]]
    (d/since
     (d/db conn)
     (d/tx->t 13194139534356)))


  )







(ns breakout3.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Initial Data

(def init-data
  {:counters
   [{:id 0 :count 0}
    {:id 1 :count 0}
    {:id 0 :count 0}]})

;; -----------------------------------------------------------------------------
;; Parsing

(defmulti read om/dispatch)

(defmethod read :counters
  [env key params]
  (let [st @(:state env)]
    {:value (into [] (map #(get-in st %))
              (get st key))}))

(defmulti mutate om/dispatch)

(defmethod mutate 'counter/increment
  [env key {:keys [id] :as params}]
  {:action
   (fn []
     (swap! (:state env) update-in
       (conj [:counter/by-id id] :count) inc))})

(def parser1
  (om/parser
    {:read   read
     :mutate mutate}))

;; -----------------------------------------------------------------------------
;; Components

(defui Counter
  static om/Ident
  (ident [this {:keys [id]}]
    [:counter/by-id id])
  static om/IQuery
  (query [this]
    [:id :count])
  Object
  (render [this]
    (let [{:keys [id count] :as props} (om/props this)]
      (dom/div nil
        (dom/p nil (str "Count: " count))
        (dom/button
          #js {:onClick
               (fn [_]
                 (om/transact! this
                   `[(counter/increment {:id ~id})]))}
          "Click Me!")))))

(def counter (om/factory Counter {:keyfn :id}))

(defui App
  static om/IQuery
  (query [this]
    [{:counters (om/get-query Counter)}])
  Object
  (render [this]
    (let [props (om/props this)]
      (apply dom/ul nil
        (map #(dom/li nil (counter %))
          (:counters props))))))

(defonce reconciler
  (om/reconciler
    {:state  init-data
     :parser parser1}))

(om/add-root! reconciler
  App (gdom/getElement "app"))

(def app-state (atom (om/normalize App init-data true)))
(def parser2 (om/parser {:read read :mutate mutate}))

(comment
  ;; Exercise 1: parse the following mutation and deref the app-state
  (parser2 {:state app-state} '[(counter/increment {:id 0})])
  @app-state
  ;; Exercise 2: Copy a UUID from the JavaScript Developer Console and use
  ;;   this to read a previous state of the application
  (om/from-history reconciler #uuid "13d69c15-e2e7-4256-89c6-855c4390228d")
  )
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
    {:value (into [] (map #(get-in st %)) (get st key))}))

(defmulti mutate om/dispatch)

(defmethod mutate 'counter/increment
  [env key params]
  {:action
   (fn []
     (swap! (:state env) update-in
       (conj (:ref env) :count) inc))})

(def parser
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
    [:id :count :rkey])
  Object
  (render [this]
    (let [{:keys [count] :as props} (om/props this)]
      (dom/div nil
        (dom/p nil (str "Count: " count))
        (dom/button
          #js {:onClick
               (fn [_]
                 (om/transact! this '[(counter/increment)]))}
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

(def reconciler
  (om/reconciler
    {:state  init-data
     :parser parser}))

(om/add-root! reconciler
  App (gdom/getElement "app"))

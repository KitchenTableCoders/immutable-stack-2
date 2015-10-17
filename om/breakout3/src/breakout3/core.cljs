(ns breakout3.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

(enable-console-print!)

(defmulti mutate om/dispatch)

(defmethod mutate ‘increment
  [env key params]
  {:action
   (fn []
     (swap! (:state env)
       update-in [:count] inc)))

(def parser
  (om/parser
    {:read read
     :mutate mutate}))

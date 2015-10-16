(ns breakout1.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Hello
  Object
  (render [this]
    (dom/div nil
      "Hello, world!")))

(def hello (om/factory Hello))

(js/ReactDOM.render (hello) (gdom/getElement "app"))

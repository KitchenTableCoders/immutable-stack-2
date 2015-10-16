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

;; Exercise 1: Change the text to something else
;; Exercise 2: Change the background color to something else
;; Exercise 3: Pass the text as props
;; Bonus: Render a list of of Hello components
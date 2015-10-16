(ns breakout1.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(defui Hello
  Object
  (render [this]
    (dom/div nil "Hello, world!")))

(def hello (om/factory Hello))

(js/ReactDOM.render (hello) (gdom/getElement "app"))

;; Exercise 1: Change the text to something else
;; Exercise 2: Change the background color to something else
;; Exercise 3: Pass the text as props and use this
;; Exercise 4: Add componentDidMount life cycle method, print something
;;   Make a change
;; Exercise 5: Add componentWillUpdate life cycle method, print something
;;   Make a change, why doesn't this print?
;; Exercise 6: Add ^:once before Hello
;; Exercise 7: Change the props, which life cycle methods print, which ones
;;   don't? Can you explain why?
;; Bonus: Render a list of of Hello components
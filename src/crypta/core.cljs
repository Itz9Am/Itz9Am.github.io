(ns crypta.core
  "Crypta" 
  (:require [clojure.string :as str]))

(def key-vector [-1 0 -3 2 1 -3 -1 1 5 -7 -4 1 -4 1 -4 1 -4 -8 6 -5 2 -6 -2 5 0 4 8 -7])

(def inv-key (map - key-vector))

(def num->alpha (zipmap (range 29)
                        "abcdefghijklmnopqrstuvwxyzåäö"))
(def alpha->num (zipmap "abcdefghijklmnopqrstuvwxyzåäö"
                        (range 29)))

(defn code-letter [letter key-digit] 
    (num->alpha (mod (+ 29 (alpha->num letter) key-digit) 29)))

(defn code-msg [msg key]
  (when (seq msg)
    (let [letter (first msg)
          key-digit (first key)]
      (if (= letter \space)
        (cons letter (code-msg (rest msg) key))
        (cons (code-letter letter key-digit)
              (code-msg (rest msg) (rest key)))))))

(defn encode [input] (apply str (code-msg (str/lower-case input) (cycle key-vector))))

(defn decode [code] (apply str (code-msg (str/lower-case code) (cycle inv-key))))

;; HTML helpers

(defn attach-handler! [element-id handler]
  (set! (.-onchange (js/document.getElementById element-id))
        (fn [event] (-> event .-target .-value handler))))

(defn set-value! [element-id value]
  (set! (.-value (js/document.getElementById element-id)) value))

;; Handlers

(defn on-encode [arg]
  (set-value! "decode" (encode arg)))

(defn on-decode [arg]
  (set-value! "encode" (decode arg)))

(attach-handler! "encode" on-encode)
(attach-handler! "decode" on-decode)

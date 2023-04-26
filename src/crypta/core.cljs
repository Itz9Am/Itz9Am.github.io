(ns crypta.core
  "Crypta" 
  (:require [clojure.string :as str]))

(def key-vector (atom []))
(defn inv-key []
  (map - @key-vector))

(def hide-key-state (atom true))

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

(defn encode [input] (apply str (code-msg (str/lower-case input) (cycle @key-vector))))

(defn decode [code] (apply str (code-msg (str/lower-case code) (cycle (inv-key)))))

;; HTML helpers

(defn attach-input-handler! [element-id handler]
  (set! (.-oninput (js/document.getElementById element-id))
        (fn [event] (-> event .-target .-value handler))))

(defn attach-change-handler! [element-id handler]
  (set! (.-onchange (js/document.getElementById element-id))
        (fn [event] (-> event .-target .-value handler))))

(defn attach-click-handler! [element-id handler]
  (set! (.-onclick (js/document.getElementById element-id))
        (fn [event] (-> event .-target .-value handler))))

(defn set-value! [element-id value]
  (set! (.-value (js/document.getElementById element-id)) value))

(defn get-value [element-id]
  (.-value (js/document.getElementById element-id)))

(defn copy-to-clipboard! [value]
  (js/navigator.clipboard.writeText value))

(defn parse-long [s]
  (js/Number.parseInt s))

(defn store! [name value] 
  (js/localStorage.setItem name (prn-str value)))

(defn load! [name]
  (read-string (js/localStorage.getItem name)))

;; Handlers

(defn on-encode [arg]
  (set-value! "decode" (encode arg)))

(defn on-decode [arg]
  (set-value! "encode" (decode arg)))

(defn on-change [arg]
  (copy-to-clipboard! (get-value "decode"))
  (set-value! "decode" "")
  (set-value! "encode" ""))

(defn on-key [value]
  (reset! key-vector (mapv parse-long (str/split value #" ")))
  (store! "key" @key-vector))

(defn on-hide-key [] 
  (swap! hide-key-state not)
  (prn hide-key-state)
  (if @hide-key-state
    (set-value! "key" "* * * * * * * *")
    (set-value! "key" (str/join " " @key-vector))))

(attach-input-handler! "encode" on-encode)
(attach-input-handler! "decode" on-decode)
(attach-change-handler! "encode" on-change)
(attach-change-handler! "decode" on-change)
(attach-change-handler! "key" on-key)
(attach-click-handler! "hide-key" on-hide-key)
(reset! key-vector (load! "key"))
(set-value! "key" "* * * * * * * *")


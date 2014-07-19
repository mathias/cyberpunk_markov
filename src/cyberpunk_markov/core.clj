(ns cyberpunk-markov.core
  (:require [markov.core :refer :all]
            [clojure.java.io :as io]
            [opennlp.nlp :refer :all]))

(def combined-books (atom ""))
(def neuromancer-map (atom {}))
(def accelerando-map (atom {}))

;; (->>
;;  (build-from-file 2 (io/resource "neuromancer.txt"))
;;  (reset! neuromancer-map))

;; (->>
;;  (build-from-file 2 (.io/resource "accelerando.txt"))
;;  (reset! accelerando-map))

(defn load-resource
 [filename]
 (slurp (io/file (io/resource filename))))

(defn load-books []
  (reset! combined-books
          (str (load-resource "accelerando.txt")
               "\n\n"
               (load-resource "neuromancer.txt"))))

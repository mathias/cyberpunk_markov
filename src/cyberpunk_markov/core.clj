(ns cyberpunk-markov.core
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [markov.core :refer :all]
            [opennlp.nlp :refer :all]
            [opennlp.treebank :refer :all]))

;;(def combined-books (atom ""))
;;(def neuromancer-map (atom {}))
;;(def accelerando-map (atom {}))

;; (->>
;;  (build-from-file 2 (io/resource "neuromancer.txt"))
;;  (reset! neuromancer-map))

;; (->>
;;  (build-from-file 2 (.io/resource "accelerando.txt"))
;;  (reset! accelerando-map))

(defn load-resource
 [filename]
 (slurp (io/file (io/resource filename))))

;; (defn load-books []
;;   (reset! combined-books
;;           (str (load-resource "accelerando.txt")
;;                "\n\n"
;;                (load-resource "neuromancer.txt"))))

;;(def accelerando (load-resource "accelerando.txt"))
;;(def neuromancer (load-resource "neuromancer.txt"))

; open nlp setup

(def get-sentences (make-sentence-detector "resources/models/en-sent.bin"))
(def tokenize (make-tokenizer "resources/models/en-token.bin"))
(def detokenize (make-detokenizer "resources/models/english-detokenizer.xml"))
(def pos-tag (make-pos-tagger "resources/models/en-pos-maxent.bin"))
(def name-find (make-name-finder "resources/models/en-ner-person.bin"))
(def chunker (make-treebank-chunker "resources/models/en-chunker.bin"))

;; (def mapped-accel
;;   (->>
;;    (load-resource "accelerando.txt")
;;    tokenize
;;    (take 100)
;;    pos-tag
;;    build-from-coll))

;;(take 100 (generate-walk "sky" mapped-accel))

(defn words-from-pos
  "Get the words back from a parts-of-speech collection"
  [coll]
  (map first coll))

;; (defn build-books-markov
;;   []
;;   (->>
;;    @combined-books
;;    tokenize
;;    pos-tag
;;    build-from-coll))

(defn paragraphs-from
  [corpus]
  (split corpus #"\n"))

(defn take-paragraphs
  [num corpus]
  (take num (paragraphs-from corpus)))

;; (def markov (build-books-markov))

;; (detokenize (words-from-pos (take 100 (generate-walk [["Manfred" "NNP"]] markov))))

(defn nth-paragraphs
  [corpus num]
  (nth (paragraphs-from corpus) num))

;;(def split-neuromancer (paragraphs-from neuromancer))

(defn cleanup-neuromancer
  "Cleanup some issues with formatting of neuromancer.txt"
  [corpus]
  (-> corpus
      (clojure.string/replace #"`" "\"")
      (clojure.string/replace #"' " "\" ")
      (clojure.string/replace #"-\n" "")
      (clojure.string/replace #"\n\s\s" "\t")
      (clojure.string/replace #"\n" " ")
      (clojure.string/replace #"\t" "\n")))

;;(def cleaned-neuromancer (cleanup-neuromancer neuromancer))
;;(def neuromancer-paragraphs (paragraphs-from cleaned-neuromancer))
;;(def accelerando-paragraphs (paragraphs-from accelerando))

;;(def markovs-by-para (map (fn [p1 p2] (->> (str p1 "\n" p2) tokenize pos-tag build-from-coll)) accelerando-paragraphs neuromancer-paragraphs))

;; word counts per paragraph
;; (map #(count (clojure.string/split % #" ")) neuromancer-paragraphs)

(defn mean [coll]
  (if (empty? coll)
      0
      (/ (reduce + coll) (count coll))))

(defn mean-int [coll]
  (int (mean coll)))

;; average words per paragraph for each book:
;; (mean-int (map #(count (clojure.string/split % #" ")) neuromancer-paragraphs))
;;=> 31
;; (mean-int (map #(count (clojure.string/split % #" ")) accelerando-paragraphs))
;;=> 52

;; IGNORE:
;; (defmacro fn-name
;;   [f]
;;   `(-> ~f var meta :name str))

;; (defn print-progress [func input]
;;   (do
;;     (print (str (fn-name func) " got " (count input) " items."))
;;     (func input)))

;; (defn intercept [input]
;;   (println "...")
;;   input)

(defn markov-from-books []
  (->
   (str (load-resource "accelerando.txt")
        "\n"
        (cleanup-neuromancer (load-resource "neuromancer.txt")))
   tokenize
   pos-tag
   build-from-coll))

;; (def markov-parsed (markov-from-books))

;; (def corpus
;;   (str (load-resource "accelerando.txt")
;;        "\n"
;;        (cleanup-neuromancer (load-resource "neuromancer.txt"))))
;; (count (paragraphs-from corpus))
;;=> 5513
;; (def tokenized-corpus (tokenize corpus))
;; (def tagged-corpus (pos-tag tokenized-corpus))

;; (defn generate-paragraphs
;;   [n]
;;   (let [avg-words 52]
;;     (repeatedly n (take (rand avg-words) (generate-walk markov-parsed)))))

;; Calling pos-tag on the entire corpus (2 books) takes too long!
;; Instead we need an async pos-tag that collects all the tagged data at the end


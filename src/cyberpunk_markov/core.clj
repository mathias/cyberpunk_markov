(ns cyberpunk-markov.core
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [markov.core :refer :all]
            [opennlp.nlp :refer :all]
            [opennlp.treebank :refer :all]))


(defn load-resource
 [filename]
 (slurp (io/file (io/resource filename))))

; open nlp setup

(def get-sentences (make-sentence-detector "resources/models/en-sent.bin"))
(def tokenize (make-tokenizer "resources/models/en-token.bin"))
(def detokenize (make-detokenizer "resources/models/english-detokenizer.xml"))
(def pos-tag (make-pos-tagger "resources/models/en-pos-maxent.bin"))
(def name-find (make-name-finder "resources/models/en-ner-person.bin"))
(def chunker (make-treebank-chunker "resources/models/en-chunker.bin"))

(defn words-from-pos
  "Get the words back from a parts-of-speech collection"
  [coll]
  (map first coll))

(defn paragraphs-from
  [corpus]
  (split corpus #"\n"))

(defn take-paragraphs
  [num corpus]
  (take num (paragraphs-from corpus)))

;; (detokenize (words-from-pos (take 100 (generate-walk [["Manfred" "NNP"]] markov))))

(defn nth-paragraphs
  [corpus num]
  (nth (paragraphs-from corpus) num))

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

(defn word-counts-per-paragraph
  [corpus]
  (map #(count (clojure.string/split % " ")) (paragraphs-from corpus)))

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

(defn markov-from-books []
  (->
   (str (load-resource "accelerando.txt")
        "\n"
        (cleanup-neuromancer (load-resource "neuromancer.txt")))
   tokenize
   pos-tag
   build-from-coll))

;; Warning: This takes awhile:
;; (def markov-parsed (markov-from-books))

;; Calling pos-tag on the entire corpus (2 books) takes too long!
;; Instead we need an async pos-tag that collects all the tagged data at the end

(defn tagged-corpus [corpus]
  (pmap pos-tag (map tokenize (paragraphs-from corpus))))

;; Generating novels:

(defn split-paragraphs
  [corpus]
  (clojure.string/split corpus #"\n"))

(defn split-spaces
  [s]
  (clojure.string/split s #" "))

(defn average-words-per-sentence
  "Calculate the average words per sentence from our source material."
  [corpus]
  (let [sentences (clojure.string/split corpus #"[\.]")
        words-by-sentence (map split-spaces sentences)
        words-per (map count words-by-sentence)]
    (mean-int words-per)))

(defn average-sentences-per-paragraph
  "Calculate the average words per sentence from our source material."
  [corpus]
  (let [paragraphs (split-paragraphs corpus)
        num-paragraphs (count paragraphs)
        split-sentences #(clojure.string/split % #"[\.]")
        sentences-per-para (map (comp count split-sentences) paragraphs)]
    (mean-int sentences-per-para)))

(defn generate-sentence
  [markov-model average-words-per-sentence]
  (let [length (+ (rand-int average-words-per-sentence) 1) ;; range at least 1 to average-words-per-sentence
        tagged-words (take average-words-per-sentence (generate-walk markov-model))
        just-words (map first tagged-words)
        joined-words (clojure.string/join " "  just-words)]
    joined-words))

(defn sentences
  [markov-model avg-words-per-sentence]
  (repeatedly #(generate-sentence markov-model avg-words-per-sentence)))

(defn paragraphs
  [markov-model avg-sentences-per-para avg-words-per-sentence]
   (repeatedly #(take avg-sentences-per-para (sentences markov-model avg-words-per-sentence))))

(defn generate-paragraphs
  "Generate n number of paragraphs with average number of sentences per paragraph."
  [num-paras markov-model avg-sentences-per-para avg-words-per-sentence]
  (let [paras (take num-paras (paragraphs markov-model avg-sentences-per-para avg-words-per-sentence))
        joined-sentences (map #(clojure.string/join ". " %) paras)
        joined-paras (clojure.string/join "\n\n" joined-sentences)]
    joined-paras))

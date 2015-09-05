(defproject cyberpunk_markov "0.1.0-SNAPSHOT"
  :description "Parts-of-speech tagged Markov Chains to have the computer write a novella"
  :url "https://github.com/mathias/cyberpunk_markov"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [janiczek/markov "0.3.0"]
                 [clojure-opennlp "0.3.2"]]
  :jvm-opts ["-Xmx2g"]
  :global-vars {*print-length* 7
                *print-level* 7})

# cyberpunk_markov

Parts-of-speech tagged Markov Chains to have the computer write a novella.

Given a corpus (in this case, two novels), tag the parts-of-speech with OpenNLP and then generate Markov chains with the parts-of-speech tagging taken into account. The theory is that this will result in slightly better output than basic Markov chains.

## Usage

See `/src/cyberpunk_markov/core.clj` for current status to run REPL. This is not a standalone project yet; it is more of a sketch.

### Setup

Download the following files from the opennlp models page:

* `en-chunker.bin`
* `en-ner-date.bin`
* `en-ner-location.bin`
* `en-ner-money.bin`
* `en-ner-organization.bin`
* `en-ner-percentage.bin`
* `en-ner-person.bin`
* `en-ner-time.bin`
* `en-pos-maxent.bin`
* `en-pos-perceptron.bin`
* `en-sent.bin`
* `en-token.bin`
* `english-detokenizer.xml`

And place them in `resources/models`

Put your books in `resources/` and use the `load-resource` fn to load them.

Good luck.

## License

Copyright © 2014 Matt Gauger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# Hidden Markov Model

This is a simulation of the Hidden Markov Model as it is applied in the field of automated speech recognition. The model accepts a String sequence of observations of vocabulary {"1", "2", "3"} and computes the probability of observation sequences (likelihoods) and then decodes the input to produce the hidden state sequence.

## Installation Instructions

*	Simply clone the hashbasedtable branch as follows: 
	```
	git clone -b hashbasedtable https://github.com/dvijayak/HiddenMarkovModel.git
	```
	### OR
	Download a ZIP archive of the source from here: https://github.com/dvijayak/HiddenMarkovModel/tree/hashbasedtable
	
*	Compile and Run as follows:
	```
	javac -d bin -sourcepath src -cp libs/guava-13.0.1.jar src/markov/HiddenMarkovModel.java
	java -cp bin;libs/guava-13.0.1.jar markov.HiddenMarkovModel
	```

*	Optional Command-Line Arguments	
	The program accepts the input sequence from stdin. You also have the option of inputing the String as a command-line argument
	
## Caution

PLEASE only use the vocabulary {"1", "2", "3"}. The model does not reject observation that do not exist in the vocabulary YET. Your application will crash if you do not heed this warning.
	

 
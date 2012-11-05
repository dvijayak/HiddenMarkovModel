package markov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class HiddenMarkovModel 
{	
	private ArrayList<String> states;
	private String hiddenStateSequence;
	private int N; // Number of states
	
	private ArrayList<String> v; // Vocabulary of observations
	private int V; // Number of possible observations
	private ArrayList<String> o; // Sequence of observations (max 20)	
	private int T; // Number of observations (given sequence)	
	
	// Probabilities Tables/Matrices
	private Table<String, String, Double> transitions;
	private Table<String, String, Double> emissions;
	private Table<String, Integer, Double> alpha;
	private Table<String, Integer, Double> viterbi;
	
	// Backtrace pointer
	private Map<String, String> backTrace;
	
	public HiddenMarkovModel () 
	{		
		/* Default parameters */
		
		// States (minus START and END)
		states = new ArrayList<String>();		
		states.add("HOT");
		states.add("COLD");	
		N = states.size(); // discard START and END in the count of states
		
		// Vocabulary		
		v = new ArrayList<String>();
		v.add("1");
		v.add("2");
		v.add("3");
		V = v.size(); // possible values are 1, 2 and 3	
		
		// Transition probabilities table/matrix
		transitions = HashBasedTable.create(N, N); // Dimensions N x N
		transitions.put("START", "HOT", 0.8); // P(H|START)
		transitions.put("START", "COLD", 0.2); // P(C|START)
		transitions.put("HOT", "HOT", 0.7); // P(H|H)
		transitions.put("HOT", "COLD", 0.3); // P(C|H)
		transitions.put("COLD", "HOT", 0.4); // P(H|C)
		transitions.put("COLD", "COLD", 0.6); // P(C|C)
				
		// Emission probabilities table/matrix
		emissions = HashBasedTable.create(V, N); // Dimensions V x N
		emissions.put("1", "HOT", 0.2); // P(1|H)
		emissions.put("1", "COLD", 0.5); // P(1|C)
		emissions.put("2", "HOT", 0.4); // P(2|H)
		emissions.put("2", "COLD", 0.4); // P(2|C)
		emissions.put("3", "HOT", 0.4); // P(3|H)
		emissions.put("3", "COLD", 0.1); // P(3|C)				
		
	}

	// Construct the likelihood probabilities table/matrix using the Forward algorithm
	// Returns P(O|lambda)
	private void buildLikelihoodTable (ArrayList<String>obs)
	{	
		o = obs;
		T = o.size();
		
		// Initialize likelihood probabilities						
		alpha = HashBasedTable.create(N + 2, T); 
		for (String state : states)			
			alpha.put(state, 0, transitions.get("START", state) * emissions.get(o.get(0), state));										
		
		// Populate the table
		for (int t = 1; t < T; t++) // Step through each observation starting from the second one, i.e. t > 0
		{			
			for (String j : states) // Step through each state
			{
				// Likelihood computation
				double likelihood = 0;
				for (String i : states)
				{
					likelihood += alpha.get(i, t - 1) * transitions.get(i, j) * emissions.get(o.get(t), j);
				}
				alpha.put(j, t, likelihood);
			}			
		}
		
		// Termination
		double termination = 0;
		for (String i : states)
			termination += alpha.get(i, T - 1);
					
		alpha.put("END", T - 1, termination); // P(O|lambda)								
	}
	
	// Construct the decoder probabilities table/matrix using the Viterbi algorithm
	// Returns P* and hidden state sequence*	
	private void buildViterbiTable (ArrayList<String>obs)
	{	
		o = obs;
		T = o.size();
		
		// Initialize viterbi probabilities and back-trace						
		viterbi = HashBasedTable.create(N + 2, T); 
		backTrace = new HashMap<String, String>();
		for (String state : states)
		{
			viterbi.put(state, 0, transitions.get("START", state) * emissions.get(o.get(0), state));			
			backTrace.put(state, state);
		}
		
		// Populate the table
		for (int t = 1; t < T; t++) // Step through each observation starting from the second one, i.e. t > 0
		{		
			Map<String, String> backpointer = new HashMap<String, String>();
			for (String j : states) // Step through each state
			{
				// max & argmax computation
				double maximum = 0;
				String argMaximum = j;
				for (String i : states)
				{
					double interim = viterbi.get(i, t - 1) * transitions.get(i, j) * emissions.get(o.get(t), j);
					if (interim > maximum)
					{
						maximum = interim;
						argMaximum = i;
					}
				}
				viterbi.put(j, t, maximum);				
				backpointer.put(j, backTrace.get(argMaximum) + "->" + j); 
			}
			backTrace = backpointer;
		}
				
		// Termination
		double pStar = 0;
		String btStar = states.get(0); // btStar is the state of the cell with the highest score
		for (String i : states)
		{
			double interim = viterbi.get(i, T - 1);
			if (interim > pStar) 
			{
				pStar = interim;
				btStar = i;
			}
		}					
		
		viterbi.put("END", T - 1, pStar);					
		hiddenStateSequence = backTrace.get(btStar);		
	}
		

	
	public String printTable (Table<String, Integer, Double> table)
	{
		String output = "";
		
		// Print the header columns
		output += "\t\t";
		for (String obs : o)
			output += obs + "\t\t\t";
		output += "\r\n";
		
		for (String state : states)
		{
			if (!state.equalsIgnoreCase("START") && !state.equalsIgnoreCase("END"))
			{
				// Print the header rows
				output += state + "\t"; 
				for (int t = 0; t < T; t++)				
				{
					output += table.get(state, t) + "\t";
				}
				output += "\r\n";	
			}			
		}
			
		return output;
	}
	
	public static void main(String[] args) throws IOException 
	{			
		System.out.println("\r\nWelcome to this Hidden Markov Model simulator!");
		
		String input = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
		while (true)
		{			
			if (args.length == 0)
			{				   
				System.out.println("\r\nPlease enter a sequence of integer observations from the vocabulary V = {1, 2, 3}");			
			    input = br.readLine();		    
			}
			else
				input = args[0]; // Take input from args instead if present
			
		    if (input != null)
		    {
		    	// Exit condition
		    	if (input.equalsIgnoreCase("q"))
		    		break;
		   
		    	// Parse the input sequence
			    ArrayList<String> obs = new ArrayList<String>();
		    	for (int i = 0; i < input.length(); i++)
		    	{
		    		char[] c = {input.charAt(i)};		    		
		    		obs.add(new String(c));
		    	}
		   
		    	// Create the Hidden Markov Model
			    HiddenMarkovModel HMM = new HiddenMarkovModel();		
			    /* for (String o : obs) // TODO: Check if observations are in vocabulary
			    	for (String v : HMM.v)
			    		if (!o.equals(v))
			    			continue; */
			    HMM.buildLikelihoodTable(obs);		 // Likelihood computation using Forward algorithm			    			    
				HMM.buildViterbiTable(obs);		 // Decoding computation using Viterbi algorithm
			    
			    /* Display output */
			    
			    // Forward Probabilities
			    String output = "\r\nThe likelihood computations for the given input observations sequence ";
			    for (String o : HMM.o)				
					output += o + " ";
				output += " are:\r\n";
			    output += HMM.printTable(HMM.alpha);
			    output += "The termination P(O|lambda) = " + HMM.alpha.get("END", HMM.T - 1) + "\r\n";
			    System.out.print(output);
			    bw.write(output);
			    
 			    // Viterbi Probabilities
			    output = "\r\nThe decoder computations for the given input observations sequence ";
			    for (String o : HMM.o)				
					output += o + " ";
				output += " are:\r\n";
			    output += HMM.printTable(HMM.viterbi);		
			    output += "The termination P* (Highest Score) = " + HMM.viterbi.get("END", HMM.T - 1) + "\r\n";
			    output += "DECODING COMPLETE: The hidden state sequence is = " + HMM.hiddenStateSequence + "\r\n";
			    System.out.print(output);
			    bw.write(output);		
			    
			    System.out.println("\r\n==========================Return==========================\r\n");
			    bw.write("\r\n==========================Return==========================\r\n");
		    }
		}		
		System.out.println("\r\n======================End of Program======================\r\n");
		bw.write("\r\n======================End of Program======================\r\n");
		bw.flush();
		br.close();
		bw.close();
				
	}
}

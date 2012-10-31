package markov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.HashBasedTable;

public class HiddenMarkovModel 
{	
	private ArrayList<String> states;
	private ArrayList<String> hiddenStates;
	private int N; // Number of states
	
	private ArrayList<String> v; // Vocabulary of observations
	private int V; // Number of possible observations
	private ArrayList<String> o; // Sequence of observations (max 20)	
	private int T; // Number of observations (given sequence)	
	
	// Probabilities
	private HashBasedTable<String, String, Double> transitions;
	private HashBasedTable<String, String, Double> emissions;
	private HashBasedTable<String, String, Double> alpha;
	private HashBasedTable<String, String, Double> viterbi;
	
	private Map<String, Double> start;
/*	private double[][] a;
	private double[][] b;
	private double[][] alpha;	
	private double obsGivenLambda; // P(O|lambda) = alpha(END, T)
	private double[][] viterbi;*/		
	
	// Back-tracing pointers
	private double[][] bp;
	private LinkedList<Integer> backTrace;
	
	public HiddenMarkovModel () 
	{		
		/* Default parameters */
		
		// States (minus START and END)
		states = new ArrayList<String>();		
//		states.add("START");
		states.add("HOT");
		states.add("COLD");	
//		states.add("END");
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

	/*// Construct the decoding probabilities table/matrix using the Viterbi algorithm
	private void buildViterbiTable ()
	{
		// Initialize likelihood  & viterbi probabilities
		alpha = new double[(N + 2) + 1][(T) + 1];
		viterbi = new double[(N + 2) + 1][(T) + 1];
		for (int j = 1; j <= N; j++)
		{			
			for (int t = 1; t <= T; t++)
			{		
				if (t != 1)
				{
					alpha[j][t] = 0.0;
					viterbi[j][t] = 0.0;
				}
				else
				{
					alpha[j][1] = a[0][j] * b[o[1]][j];
					viterbi[j][1] = a[0][j] * b[o[1]][j];
				}
			}	
		}				
		
		 Populate the table 
		
		bp = new double[(N + 2) + 1][(T) + 1];
		backTrace = new LinkedList<Integer>();
		for (int t = 2; t <= T; t++)		
		{						
			for (int j = 1; j <= N; j++)
			{				
				int maxState = 1; // The state which produced the maximum for the Viterbi computation
				for (int i = 1; i <= N; i++)
				{			
					// Likelihood computation
					double forward = alpha[i][t - 1] * a[i][j] * b[o[t]][j];					
					alpha[j][t] += forward;					
					
					// Viterbi probabiity computation
					double decoding = viterbi[i][t - 1] * a[i][j] * b[o[t]][j];
					if (i == 1)
					{
						viterbi[j][t] = decoding;
						maxState = i;
						bp[j][t] = i;
					}
					else 
						if (decoding > viterbi[j][t]) // This is an implementation of a Max function
						{
							viterbi[j][t] = decoding;
							maxState = i;
							bp[j][t] = i;
						}
					
					// Termination
					if (t == T)
					{
						
					}
				}		
//				if (j != N + 1)
				backTrace.add(new Integer(maxState));				
			}

		}	
	}*/
	
	// Construct the likelihood probabilities table/matrix using the Forward algorithm
	private double buildLikelihoodTable (ArrayList<String>obs)
	{	
		o = obs;
		T = o.size();
		
		// Initialize likelihood probabilities		
		alpha = HashBasedTable.create(N + 2, T);
		for (String state : states)
			alpha.put(state, o.get(0), transitions.get("START", state) * emissions.get(o.get(0), state));										
		
		// Populate the table
		for (int t = 1; t < T; t++) // Step through each observation starting from the second one, i.e. t > 0
		{			
			for (String j : states) // Step through each state
			{
				// Likelihood computation
				double likelihood = 0;
				for (String i : states)
				{
					likelihood += alpha.get(i, o.get(t - 1)) * transitions.get(i, j) * emissions.get(o.get(t), j);
				}
				alpha.put(j, o.get(t), likelihood);
			}			
		}
		
		double termination = 0;
		for (String i : states)
			termination += alpha.get(i, o.get(T - 1));
					
		alpha.put("END", o.get(T - 1), termination);
		System.out.println("END IS " + termination);
		
/*		// Populate the table			
		for (int t = 1; t < T; t++)	// Step through each time (starting from the second) step/event/observation	
		{						
			for (int j = 0; j < N; j++) // Step through each state
			{								
				for (int i = 1; i <= N; i++)
				{			
					// Likelihood computation					
					alpha[j][t] += alpha[i][t - 1] * a[i][j] * b[(o[t])-1][j];
				}		
			}

		}
		*/
		
		
		return termination;
	}
	
	public String printTable (HashBasedTable<String, String, Double> table)
	{
		String output = "";
		
		// Print the header columns
		output += "\t\t";
		for (String obs : o)
			output += obs + "\t\t\t";
		output += "\n";
		
		for (String state : states)
		{
			if (!state.equalsIgnoreCase("START") && !state.equalsIgnoreCase("END"))
			{
				// Print the header rows
				output += state + "\t"; 
				for (String obs : o)
				{
					output += table.get(state, obs) + "\t";
				}
				output += "\n";	
			}			
		}
			
		return output;
	}
	
	public static void main(String[] args) throws IOException 
	{			
		System.out.println("\nWelcome to this Hidden Markov Model simulator!");
		
		String input = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
		while (true)
		{			
			if (args.length == 0)
			{				   
				System.out.println("\nPlease enter a sequence of integer observations from the vocabulary V = {1, 2, 3}");			
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
//			    for (String o : obs)
//			    	for (String v : HMM.v)
//			    		if (!o.equals(v))
//			    			continue;
			    HMM.buildLikelihoodTable(obs);		 // Likelihood computation using forward algorithm   
			    
			    /* Display output */
			    
			    // Forward Probabilities
			    String output = "\nThe likelihood computations for the given input observations sequence ";
			    for (String o : HMM.o)				
					output += o + " ";
				output += " are:\n";
			    output += HMM.printTable(HMM.alpha);		    		  
			    System.out.print(output);
			    bw.write(output);
			    
/* 			    // Viterbi Probabilities
			    output = "\nThe viterbi probabilities for the given input observations sequence ";
				for (int i = 1; i <= HMM.T; i++)
					output += HMM.o[i] + " ";
				output += " are:\n";
			    output += HMM.printTable(HMM.viterbi);		    		  
			    System.out.print(output);
			    bw.write(output);
			    
			    System.out.println("\n" + HMM.backTrace.size());
			    
			    // Back-tracing Pointers
			    output = "\nThe back-tracing pointers for the given input observations sequence ";
				for (int i = 1; i <= HMM.T; i++)
					output += HMM.o[i] + " ";
				output += " are:\n";
			    output += HMM.printTable(HMM.bp);		    		  
			    System.out.print(output);
			    bw.write(output);	 */		    			    
		    }
		}		
		System.out.println("\n======================End of Program======================\n");
		bw.write("\n======================End of Program======================\n");
		bw.flush();
		br.close();
		bw.close();
				
	}
}

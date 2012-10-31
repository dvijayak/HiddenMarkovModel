package markov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

public class HiddenMarkovModel 
{
	private ArrayList<String> q;
	private ArrayList<String> hiddenQ;
	private int N; // Number of states
	
	private int[] v; // Vocabulary of observations
	private int V; // Number of possible observations
	private int[] o; // Sequence of observations (max 20)	
	private int T; // Number of observations (given sequence)	
	
	// Probabilities
	private double[][] a;
	private double[][] b;
	private double[][] alpha;	
	private double obsGivenLambda; // P(O|lambda) = alpha(END, T)
	private double[][] viterbi;		
	
	// Back-tracing pointers
	private double[][] bp;
	private LinkedList<Integer> backTrace;
	
	public HiddenMarkovModel () 
	{		
		/* Default parameters */
		
		q = new ArrayList<String>();		
		q.add("HOT");
		q.add("COLD");		
		N = q.size();
		
		V = 3; // possible values are 1, 2 and 3	
		
		// Default transition probabilities matrix
		a = new double[N + 1][N]; // Matrix of dimensions N+1 rows and N cols		
		a[0][0] = 0.7; // P(H|H)
		a[0][1] = 0.3; // P(C|H)
		a[1][0] = 0.4; // P(H|C)
		a[1][1] = 0.6; // P(C|C)
		a[2][0] = 0.8; // P(H|START)
		a[2][1] = 0.2; // P(C|START)
		
		// Default emission probabilities matrix
		b = new double[O][N]; // Matrix of dimensions O rows x N cols
		b[0][0] = 0.2; // P(1|H)
		b[0][1] = 0.5; // P(1|C)
		b[1][0] = 0.4; // P(2|H)
		b[1][1] = 0.4; // P(2|C)
		b[2][0] = 0.4; // P(3|H)
		b[2][1] = 0.1; // P(3|C)
	}

	
	public void setParameters () 
	{							
		this.T = o.length;			
	}
	
	// Construct the decoding probabilities table/matrix using the Viterbi algorithm
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
		
		/* Populate the table */
		
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
	}
	
	// Construct the likelihood probabilities table/matrix using the Forward algorithm
	private void buildLikelihoodTable ()
	{		
		// Initialize likelihood probabilities
		alpha = new double[N + 2][T];		
		for (int j = 0; j < N; j++)			
			alpha[j][0] = a[N][j] * b[(o[0])-1][j];												
		
		// Populate the table			
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
		
		
		
		
	}
	
	public String printTable (double[][] probabilities)
	{
		String output = "";
		
		// Print the header columns
		for (String state : q)
			output += state + "\t\t";
		output += "\n";
		
		for (int t = 1; t <= T; t++)		
		{
			// Print the header rows
			output += o[t] + "\t";
			for (int j = 1; j <= N; j++)
				output += new DecimalFormat("0.000000000000000").format(probabilities[j][t]) + "\t";
			output += "\n";
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
		    
		    	// Create the Hidden Markov Model
			    HiddenMarkovModel HMM = new HiddenMarkovModel();
		    	HMM.o = new int[(input.length()) + 1];		    
		    	for (int i = 1; i < HMM.o.length; i++)		    	
		    		HMM.o[i] = input.charAt((i) - 1) - 48; // subtract 48 to convert char to true int
		    				    
			    HMM.setParameters();
			    HMM.buildLikelihoodTable();		 // Likelihood computation using forward algorithm   
			    
			    /* Display output */
			    
			    // Forward Probabilities
			    String output = "\nThe likelihood computations for the given input observations sequence ";
				for (int i = 1; i <= HMM.T; i++)
					output += HMM.o[i] + " ";
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

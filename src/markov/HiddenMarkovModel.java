package markov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class HiddenMarkovModel 
{
	public static ArrayList<String> q;
	private int N; // Number of q
	
	public static int[] o; // Sequence of observations (max 20)
	private int O; // Number of possible observations
	private int T; // Number of observations (given sequence)
	
	// Probabilities
	private double[][] a;
	private double[][] b;
	private double[][] alpha;			
	
	public HiddenMarkovModel () 
	{		
		/* Default parameters */
		
		q = new ArrayList<String>();
		q.add("START");
		q.add("HOT");
		q.add("COLD");
		q.add("END");
		N = q.size() - 2; // Accounts for the extra START and END q
		
		O = 3; // possible values are 1, 2 and 3	
		
		// Default transition probabilities matrix
		a = new double[N + 1][(N) + 1]; // Matrix of dimensions N+1 rows and N cols
		a[0][1] = 0.8; // P(H|START)
		a[0][2] = 0.2; // P(C|START)
		a[1][1] = 0.7; // P(H|H)
		a[1][2] = 0.3; // P(C|H)
		a[2][1] = 0.4; // P(H|C)
		a[2][2] = 0.6; // P(C|C)								
		
		// Default emission probabilities matrix
		b = new double[(O) + 1][(N) + 1]; // Matrix of dimensions O rows x N cols
		b[1][1] = 0.2; // P(1|H)
		b[1][2] = 0.5; // P(1|C)
		b[2][1] = 0.4; // P(1|H)
		b[2][2] = 0.4; // P(1|H)
		b[3][1] = 0.4; // P(1|H)
		b[3][2] = 0.1; // P(1|H)
	}

	
	public void setParameters () 
	{							
		this.T = (o.length) - 1;			
	}
	
	
	
	// Construct the likelihood probabilities table/matrix using the forward algorithm
	private void buildLikelihoodTable ()
	{
		
		// Initialize values
		alpha = new double[(N + 2) + 1][(T) + 1];		
		for (int j = 1; j <= N; j++) {		
			alpha[j][1] = a[0][j] * b[o[1]][j];					
		}
		System.out.println("YES" + o[1]);
		
		// Populate the table
		for (int j = 1; j <= N; j++)		
		{						
			for (int t = 2; t <= T; t++)
			{				
				for (int i = 1; i <= N; i++)
				{
					alpha[j][t] += alpha[i][t - 1] * a[i][j] * b[t][j];
				}
				
				
				
				// Recursion
//				alpha[j][t] = computeCell(j, t);					
			}
		}
		
	}
	
	private double computeCell (int j, int t)
	{
		double likelihood = 0;
		
		for (int i = 1; i <= N; i++)
		{									
//			System.out.format("%f%d and %f%d and %f%d\n", alpha[i][t-1], t, a[i][j], j, b[t][j], t);
			likelihood += alpha[i][t - 1] * a[i][j] * b[t][j];
//			likelihood += getAlpha(i, t) * a[i][j] * b[t][j];
		}
		
		return likelihood;
	}
	
	private double getAlpha (int i, int t)
	{
		if (t == 1)
			return alpha[i][t];
		else
			return getAlpha(i, t - 1);
	}
	
	public String printAlpha ()
	{
		String output = "The forward probabilities for the given input observations sequence ";
		for (int i = 1; i <= T; i++)
			output += o[i] + " ";
		output += " is:\n";
		
		for (int j = 1; j <= N; j++)
		{
			output += q.get(j) + ": ";
			for (int t = 1; t <= T; t++)
				output += alpha[j][t] + " ";
			output += "\n";
		}
				
		return output;
	}
	
	public static void main(String[] args) throws IOException 
	{			
		System.out.println("\nWelcome to this Hidden Markov Model simulator!\n");
		String input = "";
//		while (!input.equalsIgnoreCase("q"))
//		{			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		    
			System.out.println("Please enter a sequence of integer observations from the vocabulary V = {1, 2, 3}");			
		    input = in.readLine();
		    
		    if (input != null)		    
		    	HiddenMarkovModel.o = new int[(input.length()) + 1];		    
		    	for (int i = 1; i < HiddenMarkovModel.o.length; i++)		    	
		    		HiddenMarkovModel.o[i] = input.charAt((i) - 1) - 48; // subtract 48 to convert char to true int
		    	
		    HiddenMarkovModel HMM = new HiddenMarkovModel();
		    HMM.setParameters();
		    HMM.buildLikelihoodTable();		    
		    System.out.print(HMM.printAlpha());
//		}
	}
}

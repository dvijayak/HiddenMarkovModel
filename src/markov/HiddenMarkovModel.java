package markov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
		a = new double[(O) + 1][(N) + 1]; // Matrix of dimensions O rows x N cols
		b[1][1] = 0.2; // P(1|H)
		b[1][2] = 0.5; // P(1|C)
		b[2][1] = 0.4; // P(1|H)
		b[2][2] = 0.4; // P(1|H)
		b[3][1] = 0.4; // P(1|H)
		b[3][2] = 0.1; // P(1|H)
	}

	
	public void setParameters (String[] q, int[] o, double[][] a, double[][] b) 
	{
		for (int i = 0; i < q.length; i++)		
			this.q.add(q[i]);
		this.N = q.length;
		
		for (int t = 0; t < o.length; t++)		
			this.o[t] = o[t];
		this.T = o.length;		
		
		// Create the transition probabilities matrix
		for (int i = 0; i < a.length; i++) 		
			for (int j = 0; j < a[i].length; j++)
				this.a[i][j] = a[i][j];
		
		// Create the emission probabilities matrix
		for (int t = 0; t < b.length; t++)
			for (int j = 0; j < b[t].length; j++)
				this.b[t][j] = b[t][j];		
	}
	
	
	
	// Construct the likelihood probabilities table/matrix using the forward algorithm
	private void buildLikelihoodTable ()
	{
		/* Initialize values */
		alpha = new double[(N + 2) + 1][(T) + 1];
		for (int j = 1; j <= N; j++)		
			alpha[j][1] = a[0][j] * b[o[1]][j];
		
		for (int t = 1; t <= T; t++)
		{
			for (int j = 1; j <= N; j++)
			{
				alpha[j][t] = computeCell(t, j);
			}
		}
		
	}
	
	private double computeCell (int t, int j)
	{
		double likelihood = 0;
		
		for (int i = 1; i <= N; i++)
		{
			likelihood += computeCell(i, t - 1) * a[i][j] * b[t][j];
		}
		
//		alpha[j][t] = likelihood; // Cell at row j and column t
		
//		return alpha[j][t];
		return likelihood;
	}

	
	
	public static void main(String[] args) throws IOException 
	{
//		HiddenMarkovModel HMM = new HiddenMarkovModel();
		
		System.out.println("\nWelcome to this Hidden Markov Model simulator!\n");
		String input = "";
		while (!input.equalsIgnoreCase("q"))
		{			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		    
			System.out.println("Please enter a sequence of integer observations from the vocabulary V = {1, 2, 3}\n");			
		    input = in.readLine();
		    
		    if (input != null)		    
		    	HiddenMarkovModel.o = new int[input.length()];
		    	for (int i = 0; i < input.length(); i++)		    	
		    		HiddenMarkovModel.o[i] = input.charAt(i) - 48; // subtract 48 to convert char to true int		    				    			    		    				
		}
	}
}

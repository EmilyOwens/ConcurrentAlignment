import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.ResultSetMetaData;
import java.util.*;

public class SequentialAlignment {
	
	// DB variables
    private static String dbURL;
    //private static String tableName = "dnaseqs.realtable1";
//    private static String tableName = "dnaseqs.realtable2";
    private static String tableName = "dnaseqs.testtable1";
    // jdbc Connection
    private static Connection conn = null;
    private static Statement stmt = null;
    
	public SequentialAlignment() {
		
	}
	
	// Result Arrays
	
	// Stores all constructed Alignments
	public static Hashtable<String, AlignResult> storeAlignments = new Hashtable<String, AlignResult>();
	
	// Stores absolute best alignments found --> this is what the program returns
	public static List<AlignResult> finalResults= new LinkedList<AlignResult>();
	
	
	// Original input sequences
	private static char[] sequence1;
	private static char[] sequence2;

	
	// Score variables
	public static int numMatches;
	public static int numStartGaps;
	public static int numContGaps;
	
	// Currently arbitrary pts
	public static int matchScore = 5;
	public static int startGapScore = 2;
	public static int continueGapScore = 1;
	
	// Constructs original Scoring matrix --> returns 2d array 
	public static int[][] ConstructArray(String seq1, String seq2) {
		/*		
		 * 			0	S	e	q	2
		 * 			-	-	-	-	-
		 * 	 0	| 	0	0	0	0	0
		 * 	 S	| 	0	x	x	x	x
		 * 	 e	| 	0	x	x	x	x
		 * 	 q	| 	0	x	x	x	x
		 * 	 1	| 	0	x	x	x	x
		 * 
		 */
			
		
		sequence1 = seq1.toCharArray();
		sequence2 = seq2.toCharArray();

		// Check if array would exceed max array size
		// If it would
		// return emptyArray instead
		try{
			//Math.multiplyExact(sequence1.length+1, sequence2.length+1);
		} catch (ArithmeticException e) {
			//System.out.println("Caught Exception e");
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
				
		// Try to construct backtraceArray
		// If array use too much memory, as sequences are too long
		// return emptyArray instead
		try {
			
			int [][] ourArray = new int[sequence1.length + 1][sequence2.length +1];
			int [][] backtraceArray = new int[sequence1.length + 1][sequence2.length +1];
			
			for(int i=1; i < sequence1.length + 1; i++)
			{
				for(int j=1; j < sequence2.length + 1; j++)
				{
					int matchMax = Math.max(Math.max(ourArray[i-1][j], ourArray[i][j-1]), 1+ ourArray[i-1][j-1]);
					int mismatchMax = Math.max(ourArray[i-1][j], ourArray[i][j-1]);
					// MATCH
					if(sequence1[i-1] == sequence2[j-1])
					{
						ourArray[i][j] = matchMax;
						
						//BackTrace
						if (matchMax == 1+ ourArray[i-1][j-1])
						{
							// 3 = Trace from the diagonal
							backtraceArray[i][j] = 3;
						} else if (matchMax == ourArray[i][j-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2
							backtraceArray[i][j] = 2;
						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray[i][j] = 1;
						}
						
					// MISMATCH
					} else {
						ourArray[i][j] = mismatchMax;
						
						//BackTrace
						if (matchMax == ourArray[i][j-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2
							backtraceArray[i][j] = 2;
						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray[i][j] = 1;
						}
					}
					
					
				}
			}
			

			ourArray = null;
			
			
			return backtraceArray;
			
		} catch (OutOfMemoryError m) {
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
	}
	
	public static AlignResult getResult(int[][] backtrace, int i, int j, String gene, int iteration){

		//Initialize scoring
		numMatches = 0;
		numStartGaps = 0;
		numContGaps = 0;
		
		LinkedList<Character> gap1 = new LinkedList<Character>();
		LinkedList<Character> gap2 = new LinkedList<Character>();
		
		
		while(backtrace[i][j] != 0)
		{
			if(backtrace[i][j] == 3)
			{
				// Mark traversed
				backtrace[i][j] = 6;
				
				// Increase match count
				numMatches += 1;
				
				// Create alignment
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, sequence2[j-1]);
				
				// Go to next cell
				i--;
				j--;
				
			} else if (backtrace[i][j] == 2) {
				// Mark traversed
				backtrace[i][j] = 5;
				
				// Increase gap score
				if(j != sequence2.length && backtrace[i][j+1] == 5) {
					numContGaps += 1;
				} else {
					numStartGaps += 1;
				}
				
				// Create alignment
				gap1.add(0, '-');
				gap2.add(0, sequence2[j-1]);
				
				// Go to next cell
				j--;
				
			} else if (backtrace[i][j] == 1) {
				// Mark traversed
				backtrace[i][j] = 4;
				
				// Increase gap score
				if(i != sequence1.length && backtrace[i+1][j] == 4)
				{
					numContGaps += 1;
				} else {
					numStartGaps += 1;
				}
				
				// Create alignment
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, '-');
				
				// Go to next cell
				i--;
			}
		}

		// If there are gaps on the ends on the sequences, add them now
		// However, don't penalize for them
		if (i>0){
			while (i>0)
			{
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, '-');
				i--;
			}
		}
		
		if (j>0){
			while (j>0)
			{
				gap1.add(0, '-');
				gap2.add(0, sequence2[j-1]);
				j--;
			}
		}
		
		
		// Calculate Final Score
		int finalScore = (numMatches*matchScore) - (numStartGaps*startGapScore) - (numContGaps*continueGapScore);
		
		// Create new AlignResult, and place it in hash
		AlignResult result = new AlignResult(gene +" - "+ iteration, gap1, gap2, numMatches, finalScore);
		storeAlignments.put(result.geneName, result); 
		
		// Mark arrays for GC
		backtrace = null;
		gap1 = null;
		gap2 = null;
		
		String hashKey = result.geneName;
		
//		System.out.println(result.geneName);
		
		// More GC
		result = null;
		
//		System.out.println("FINAL SCORE = " + finalScore);
//		System.out.println("Aligned Sequence = " + storeAlignments.get(hashKey).seq1);
		
		// Return the specific AlignResult
		return storeAlignments.get(hashKey);
	}
	
	
	
	public static void main(String[] args){
		
		// Connect to db
		// Specify OS to distinguish between Emily's and Josh's Machines
        String os = System.getProperty("os.name");
        if (os.startsWith("Linux")){
            dbURL = "jdbc:derby:../MyDB;";
        }
        else{
            dbURL = "jdbc:derby:MyDB;";
        }
        
        // Target string is the sequence you already have
        // Aligns against all db sequences
        String target = "ATCGATCAA";
//        String target = "ATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTA";//CGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGAC";//TACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCG";//ATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGAC";//TACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAG";//GATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACGATCGATCAAGGATCTCGATACG";
        //List<AlignResult> finalResults= new ArrayList<AlignResult>();
   
        // Access db
        createConnection();
        try
        {
            stmt = conn.createStatement();
            
            // Grab everything from table of interest
            ResultSet results = stmt.executeQuery("select * from " + tableName);
            
            // While there are still more entries in DB, continue
            while(results.next() )//&& results.getInt(1) < 25)
            {
                //int id = results.getInt(1);
                String geneName = results.getString(2);
                String sequence = results.getString(3);
                
//                System.out.println(sequence);
                
//                String test1 = "ATCG";
//                String test2 = "TCAG";
                
//                System.out.println(sequence.length());
//                System.out.println(target.length());
                
                // Create initialResults list
                LinkedList<AlignResult> initialResults = new LinkedList<AlignResult>();
                
                // Split sequences into k-mers
                for (int i=0; i < (sequence.length() - target.length()); i++)
                {
//                	System.out.println("Step " + i + " of " + (sequence.length() - target.length()));
                	
                	String kMer = sequence.substring(i, i+target.length());
                	
                	// Construct backtrace array for k-mer
                	int[][] testBacktrace = ConstructArray(target, kMer);
                	
//                	int[][] testBacktrace = ConstructArray(test1, test2);

                	// If out of memory --> Probably due to very long sequences
                	if (testBacktrace.length == 0 ){
                        System.out.println("Sequence length exceeded maximum. Alignment not computed.");
                        System.out.println();
    		
                    // For sequences of acceptable length
                    } else {
                        //testResult = getResult(testBacktrace, sequence1.length, sequence2.length, gappedSeq1, gappedSeq2, "TEST", i);
                        //finalResults.add(0, testResult);
                        
                    	// Creates Alignment for k-mer
                    	AlignResult testResult = getResult(testBacktrace, sequence1.length, sequence2.length, geneName, i);
                    	
                    	//AlignResult testResult = storeAlignments.get("TEST - "+i);
                    	
                    	//System.out.println(storeAlignments.get("TEST - "+i));
                    	
                    	// GC
                        testBacktrace = null;
                        
//                        System.out.println("-------------------------------");
//                        System.out.println("New Score = ");
//                        //System.out.println(initialResults.get(0).alignmentScore);
//                		System.out.println( storeAlignments.get(testResult.geneName).alignmentScore);
//                		System.out.println("-------------------------------");
                        
                		// Initial results ---> List of highest scoring k-mer alignments for 1 specific gene
                		// Add k-mer to list if it's the first alignment calculated
                		// Or if it's score is >= to the first element of the list
                        if (initialResults.size() == 0)
                        {
                        	initialResults.add(0, storeAlignments.get(testResult.geneName));
                        } else if (storeAlignments.get(testResult.geneName).alignmentScore >= initialResults.get(0).alignmentScore) {
                        	
                        	initialResults.add(0, storeAlignments.get(testResult.geneName));
                        }
                        
                        testResult = null;
                    }
                	
                	// initialBestScore is the highest alignmentScore
                	int initialBestScore = initialResults.get(0).alignmentScore;
                	
                	// Remove all elements from the list if their alignmentScore is lower than the highest
                	for (int k=0; k <initialResults.size(); k++)
                	{
                		if (initialResults.get(k).alignmentScore < initialBestScore)
                		{
                			initialResults.remove(k);
                			k--;
                		}
                	}
                	
                	
                	
//                	System.out.println(initialResults.size());
                	
                }
                
                System.out.println("--------------------");
            	for (int l=0; l < initialResults.size(); l++)
            	{
            		System.out.println( l +" "+ initialResults.get(l).geneName + " = " + initialResults.get(l).alignmentScore);
            	}
                
                System.out.println("--------------------");
                
                // finalResults ---> The best alignment scores when considering every gene in db
                // If the scores of initialResults are >= the highest score in finalResults,
                // insert into list
                // Then, remove all elements of lesser score
                //	-----> This threshold could be altered
                //	-----> e.g. Keep all elements with 10 pts of top score
            	for (int j=0; j < initialResults.size(); j++)
            	{
            		if (finalResults.size() == 0)
            		{
            			finalResults.add(initialResults.get(j));
            		} else {
            			int finalBestScore = finalResults.get(0).alignmentScore;
            			if (initialResults.get(j).alignmentScore >= finalBestScore - 2)
            			{
            				finalResults.add(0, initialResults.get(j));
            			}
            		}
            	}
            	
            	initialResults = null;
            	int finalBestScore = 0;
            	for (int m=0; m < finalResults.size(); m++)
            	{
            		if (finalResults.get(m).alignmentScore > finalBestScore)
            		{
               		 	finalBestScore = finalResults.get(0).alignmentScore;
            		}
            	}
            	
            	
            	for (int l=0; l < finalResults.size(); l++)
            	{
            		if (finalResults.get(l).alignmentScore < finalBestScore - 2)
            		{
            			finalResults.remove(l);
            			l--;
            		}
            	}
             
            }
            
            
            for (int l=0; l < finalResults.size(); l++)
        	{
        		System.out.println( l +" "+ finalResults.get(l).geneName + " = " + finalResults.get(l).alignmentScore);
        	}
            
//            System.out.println("--------------------");
            
//            System.out.println(storeAlignments.get("Gene1 - 7338").seq2);
//            System.out.println(storeAlignments.get("Gene1 - 7339").seq2);
//            System.out.println(storeAlignments.get("Gene1 - 7340").seq2);
//            System.out.println(storeAlignments.get("Gene1 - 7341").seq2);
//            System.out.println(storeAlignments.get("Gene1 - 7342").seq2);
            
            
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        } 
		
		shutdown();
	}
	
	// DB connection stuff
	private static void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
        	//EmbeddedDriver driver = new EmbeddedDriver();
            conn = DriverManager.getConnection(dbURL); 
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
   }
   
    private static void shutdown()
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (conn != null)
            {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }           
        }
        catch (SQLException sqlExcept)
        {
            
        }

    }
	
}

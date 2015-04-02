import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentAlignment implements Runnable{
	
    private static String dbURL;
    private static String tableName = "dnaseqs.realtable1";
    // jdbc Connection
    private static Connection conn = null;
    
    private static Statement stmt = null;
    
    private static ResultSet results;
    private static String target = "ATCGATCAAGGATCTCGATACGATCGAC";//TAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGG";//AGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTAC";
    private static List<AlignResult> finalResults= new ArrayList<AlignResult>();
    private static AlignResult testResult;
    
    public int me;
    private Lock lock = new ReentrantLock();


	public ConcurrentAlignment(int newMe) {
		me = newMe;
	}
	// Original input sequences
	private static char[] sequence1;
	private static ThreadLocal<char[]> sequence2 = new ThreadLocal<char[]>();

	// Constructed aligned sequences
	public static ArrayList<Character> gappedSeq1 = new ArrayList<Character>();
	public static ArrayList<Character> gappedSeq2 = new ArrayList<Character>();
	
	// Score variables
	public static int numMatches;
	public static int numStartGaps;
	public static int numContGaps;
	
	public static int matchScore = 5;
	public static int startGapScore = 2;
	public static int continueGapScore = 1;
	
	public void run(){
        try{
            lock.lock();
            Boolean next = results.next();
            lock.unlock();
            
            while(next){
                int id = results.getInt(1);
                String geneName = results.getString(2);
                String sequence = results.getString(3);
                
                int[][] testBacktrace = ConstructArray(target, sequence);
                if (testBacktrace.length == 0 ){
                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
                    System.out.println();
        
                // For sequences of acceptable length
                } else {
                    testResult = getResult(testBacktrace, sequence1.length, sequence2.get().length, gappedSeq1, gappedSeq2);
                    finalResults.add(testResult);            
                }
                
                lock.lock();
                next = results.next();
                lock.unlock();
            }
        }
        catch (SQLException sqlExcept){
            sqlExcept.printStackTrace();
        }
        
    }
    
    
    
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
		sequence2.set(seq2.toCharArray());

		// Check if array would exceed max array size
		// If it would
		// return emptyArray instead
		try{
			//Math.multiplyExact(sequence1.length+1, sequence2.length+1);
		} 
        catch (ArithmeticException e) {
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
				
		// Try to construct backtraceArray
		// If array use too much memory, as sequences are too long
		// return emptyArray instead
		try {
			
			int [][] ourArray = new int[sequence1.length + 1][sequence2.get().length +1];
			int [][] backtraceArray = new int[sequence1.length + 1][sequence2.get().length +1];
			
			for(int i=1; i < sequence1.length + 1; i++){
				for(int j=1; j < sequence2.get().length + 1; j++){
					int matchMax = Math.max(Math.max(ourArray[i-1][j], ourArray[i][j-1]), 1+ ourArray[i-1][j-1]);
					int mismatchMax = Math.max(ourArray[i-1][j], ourArray[i][j-1]);
					// MATCH
					if(sequence1[i-1] == sequence2.get()[j-1]){
						ourArray[i][j] = matchMax;
						
						//BackTrace
						if (matchMax == 1+ ourArray[i-1][j-1]){
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
			return backtraceArray;
			
		} catch (OutOfMemoryError m) {
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
	}
	
	public static AlignResult getResult(int[][] backtrace, int i, int j, ArrayList<Character> gap1, ArrayList<Character> gap2){

		//Initialize scoring
		numMatches = 0;
		numStartGaps = 0;
		numContGaps = 0;
		
		
		while(backtrace[i][j] != 0){
			if(backtrace[i][j] == 3){
				// Mark traversed
				backtrace[i][j] = 6;
				
				// Increase match count
				numMatches += 1;
				
				// Create alignment
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, sequence2.get()[j-1]);
				
				// Go to next cell
				i--;
				j--;
				
			} else if (backtrace[i][j] == 2) {
				// Mark traversed
				backtrace[i][j] = 5;
				
				// Increase gap score
				if(j != sequence2.get().length && backtrace[i][j+1] == 5) {
					numContGaps += 1;
				} else {
					numStartGaps += 1;
				}
				
				// Create alignment
				gap1.add(0, '-');
				gap2.add(0, sequence2.get()[j-1]);
				
				// Go to next cell
				j--;
				
			} else if (backtrace[i][j] == 1) {
				// Mark traversed
				backtrace[i][j] = 4;
				
				// Increase gap score
				if(i != sequence1.length && backtrace[i+1][j] == 4){
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

		if (i>0){
			while (i>0){
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, '-');
				i--;
			}
		}
		
		if (j>0){
			while (j>0){
				gap1.add(0, '-');
				gap2.add(0, sequence2.get()[j-1]);
				j--;
			}
		}
		
		int finalScore = (numMatches*matchScore) - (numStartGaps*startGapScore) - (numContGaps*continueGapScore);
		AlignResult result = new AlignResult("Test", gap1, gap2, numMatches, finalScore);
		System.out.println("FINAL SCORE = " + finalScore);
		return result;
	}
	
	public static void main(String[] args){
		
        String os = System.getProperty("os.name");
        if (os.startsWith("Linux")){
            dbURL = "jdbc:derby:../MyDB;";
        }
        else{
            dbURL = "jdbc:derby:MyDB;";
        }
        
        createConnection();
        Thread t1 = new Thread(new ConcurrentAlignment(1));
        Thread t2 = new Thread(new ConcurrentAlignment(2));
        
        try{
            stmt = conn.createStatement();
            results = stmt.executeQuery("select * from " + tableName);
        }
        catch (SQLException sqlExcept){
            sqlExcept.printStackTrace();
        }
    
        t1.start();
        t2.start();
        try{
            t1.join();
            t2.join();
        } 
        catch (InterruptedException e) {}
           
        try{
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept){
            sqlExcept.printStackTrace();
        }
        
		shutdown();
	}
	private static void createConnection()
    {
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL); 
        }
        catch (Exception except){
            except.printStackTrace();
        }
   }
   
    private static void shutdown()
    {
        try{
            if (stmt != null){
                stmt.close();
            }
            if (conn != null){
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }           
        }
        catch (SQLException sqlExcept){
            sqlExcept.printStackTrace();
        }
    }
	
}

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Aligner implements Runnable {
	private ThreadLocal<BlockingQueue<KmerTuple>> kmerQueue = new ThreadLocal<BlockingQueue<KmerTuple>>();
	public String target;
    public ThreadLocal<String> geneName = new ThreadLocal<String>();
    
    public Lock lock = new ReentrantLock();
    
    
	// Original input sequences
	public static ThreadLocal<char[]> sequence1 = new ThreadLocal<char[]>();
	public static ThreadLocal<char[]> sequence2 = new ThreadLocal<char[]>();

	// Score variables
	public static ThreadLocal<Integer> numMatches = new ThreadLocal<Integer>();
	public static ThreadLocal<Integer> numStartGaps = new ThreadLocal<Integer>();
	public static ThreadLocal<Integer> numContGaps = new ThreadLocal<Integer>();
	
	public static int matchScore = 5;
	public static int startGapScore = 2;
	public static int continueGapScore = 1;

	
	public Aligner(BlockingQueue<KmerTuple> q, String t, String name) {
		this.kmerQueue.set(q);
		this.target = t;
        this.geneName.set(name);
    }
	
	public void run() {

		
		try 
		{
//				parent.initialResults.set(parent.initialResults.get());
			Boolean cont = true;
			while(cont){
				//System.out.println("Waiting... ");
				Thread.sleep(10);
				ThreadLocal<KmerTuple> tuple = new ThreadLocal<KmerTuple>();
                
				try{
                	tuple.set(kmerQueue.get().poll(500, TimeUnit.MILLISECONDS));
                } catch (NullPointerException n) {
                	return;
                }
				
                
				//System.out.println("Consumed " + gene.getString(2));
				
				if((KmerTuple)tuple.get() == null)
				{
//						System.out.println("Aligner done.");
					return;
				}
				
                ThreadLocal<String> kmer = new ThreadLocal<String>();
                kmer.set(tuple.get().kMer);
                ThreadLocal<Integer> i = new ThreadLocal<Integer>();
                i.set(tuple.get().i);
                
//					System.out.println("Consumed " + geneName+ " "+ i);
				
				
				
				ThreadLocal<int[][]> testBacktrace = new ThreadLocal<int[][]>();
                testBacktrace.set(ConstructArray(target, kmer.get()));
                if (testBacktrace.get().length == 0 ){
                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
                    System.out.println();
        
                // For sequences of acceptable length
                } else {
                	ThreadLocal<AlignResultConcurrent> testResult = new ThreadLocal<AlignResultConcurrent>();
                    testResult.set(getResult(testBacktrace.get(), target.length(), kmer.get().length(), geneName.get(), i.get()));
                	
                	lock.lock();
                	if (Splitter.initialResults.get().size() == 0)
                	{
                		Splitter.initialResults.get().add(testResult.get()); 
                	}
                	else if(testResult.get().alignmentScore >= Splitter.initialResults.get().get(1).alignmentScore)
                	{
                		Splitter.initialResults.get().add(testResult.get());  
                	}
                	ThreadLocal<Integer> initialBestScore = new ThreadLocal<Integer>();
                    initialBestScore.set(Splitter.initialResults.get().get(1).alignmentScore);
                	lock.unlock();
                	
                	lock.lock();
                	for(int l=1; l <= Splitter.initialResults.get().size(); l++)
                	{
                		if (Splitter.initialResults.get().get(l).alignmentScore < initialBestScore.get()) 
                		{
                			Splitter.initialResults.get().remove(Splitter.initialResults.get().get(l));
                		}
                	}
                	lock.unlock();
                	
                	Splitter.initialResults.get().cleanUp();
                	         
                }
	                
	                
	                
			}
			

		} catch(InterruptedException e) { }//e.printStackTrace(); }

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
			
		
		sequence1.set(seq1.toCharArray());
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
			
			ThreadLocal<int [][]> ourArray = new ThreadLocal<int[][]>();
			ourArray.set(new int[sequence1.get().length + 1][sequence2.get().length +1]);
			
			ThreadLocal<int [][]> backtraceArray = new ThreadLocal<int[][]>();
			backtraceArray.set(new int[sequence1.get().length + 1][sequence2.get().length +1]);
			
			for(int i=1; i < sequence1.get().length + 1; i++){
				for(int j=1; j < sequence2.get().length + 1; j++){
					int matchMax = Math.max(Math.max(ourArray.get()[i-1][j], ourArray.get()[i][j-1]), 1+ ourArray.get()[i-1][j-1]);
					int mismatchMax = Math.max(ourArray.get()[i-1][j], ourArray.get()[i][j-1]);
					// MATCH
					if(sequence1.get()[i-1] == sequence2.get()[j-1]){
						ourArray.get()[i][j] = matchMax;
						
						//BackTrace
						if (matchMax == 1+ ourArray.get()[i-1][j-1]){
							// 3 = Trace from the diagonal
							backtraceArray.get()[i][j] = 3;
						} else if (matchMax == ourArray.get()[i][j-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2
							backtraceArray.get()[i][j] = 2;
						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray.get()[i][j] = 1;
						}
						
					// MISMATCH
					} else {
						ourArray.get()[i][j] = mismatchMax;
						
						//BackTrace
						if (matchMax == ourArray.get()[i][j-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2
							backtraceArray.get()[i][j] = 2;
						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray.get()[i][j] = 1;
						}
					}
				}
			}
			return backtraceArray.get();
			
		} catch (OutOfMemoryError m) {
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
	}
	
	public static AlignResultConcurrent getResult(int[][] backtrace, int i, int j, String gene, int iteration){

		//Initialize scoring
		numMatches.set(0);
		numStartGaps.set(0);
		numContGaps.set(0);
		
		ThreadLocal<CharLazyList> gap1 = new ThreadLocal<CharLazyList>();
		ThreadLocal<CharLazyList> gap2 = new ThreadLocal<CharLazyList>();
		
		
		while(backtrace[i][j] != 0){
			if(backtrace[i][j] == 3){
				// Mark traversed
				backtrace[i][j] = 6;
				
				// Increase match count
				numMatches.set(numMatches.get()+1);
				
				// Create alignment
				gap1.get().add(sequence1.get()[i-1]);
				gap2.get().add(sequence2.get()[j-1]);
				
				// Go to next cell
				i--;
				j--;
				
			} else if (backtrace[i][j] == 2) {
				// Mark traversed
				backtrace[i][j] = 5;
				
				// Increase gap score
				if(j != sequence2.get().length && backtrace[i][j+1] == 5) {
					numContGaps.set(numContGaps.get()+1);
				} else {
					numStartGaps.set(numStartGaps.get()+1);
				}
				
				// Create alignment
				gap1.get().add('-');
				gap2.get().add(sequence2.get()[j-1]);
				
				// Go to next cell
				j--;
				
			} else if (backtrace[i][j] == 1) {
				// Mark traversed
				backtrace[i][j] = 4;
				
				// Increase gap score
				if(i != sequence1.get().length && backtrace[i+1][j] == 4){
					numContGaps.set(numContGaps.get()+1);
				} else {
					numStartGaps.set(numStartGaps.get()+1);
				}
				
				// Create alignment
				gap1.get().add(sequence1.get()[i-1]);
				gap2.get().add('-');
				
				// Go to next cell
				i--;
			}
		}

		if (i>0){
			while (i>0){
				gap1.get().add(sequence1.get()[i-1]);
				gap2.get().add('-');
				i--;
			}
		}
		
		if (j>0){
			while (j>0){
				gap1.get().add('-');
				gap2.get().add(sequence2.get()[j-1]);
				j--;
			}
		}
		
		int finalScore = (numMatches.get()*matchScore) - (numStartGaps.get()*startGapScore) - (numContGaps.get()*continueGapScore);
		AlignResultConcurrent result = new AlignResultConcurrent(gene +" - "+ iteration, gap1.get(), gap2.get(), numMatches.get(), finalScore);

//		System.out.println("FINAL SCORE = " + finalScore);
//		System.out.println("Aligned Sequence = " + gap1);
		return result;
	}
	
	
}

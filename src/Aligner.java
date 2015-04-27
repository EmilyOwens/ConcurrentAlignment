//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;

public class Aligner implements Runnable {
	private BlockingQueue<KmerTuple> kmerQueue;// = new BlockingQueue<KmerTuple>();
	public String target;
    //private ThreadLocal<String> geneName;
     
    
	// Original input sequences
	public static ThreadLocal<char[]> sequence1 = new ThreadLocal<char[]>();
	public static ThreadLocal<char[]> sequence2 = new ThreadLocal<char[]>();

//	// Score variables
//	public static ThreadLocal<Integer> numMatches = new ThreadLocal<Integer>();
//	public static ThreadLocal<Integer> numStartGaps = new ThreadLocal<Integer>();
//	public static ThreadLocal<Integer> numContGaps = new ThreadLocal<Integer>();
	
	public static int matchScore = 5;
	public static int startGapScore = 2;
	public static int continueGapScore = 1;

	
	public Aligner(BlockingQueue<KmerTuple> q, String t){//, String name) {
		this.kmerQueue = q;
		this.target = t;

        //this.geneName = new ThreadLocal<String>();
        //this.geneName.set(name);
    }
	
	public void run() {
        System.out.println("Splitter.geneName= " +Splitter.geneName.get());

		
		try 
		{
            ThreadLocal<KmerTuple> tuple = new ThreadLocal<KmerTuple>();
            ThreadLocal<String> kmer = new ThreadLocal<String>();
            ThreadLocal<Integer> i = new ThreadLocal<Integer>();
            ThreadLocal<int[][]> testBacktrace = new ThreadLocal<int[][]>();
            ThreadLocal<AlignResultConcurrent> testResult = new ThreadLocal<AlignResultConcurrent>();
            ThreadLocal<Integer> initialBestScore = new ThreadLocal<Integer>();
            initialBestScore.set(0);

//				parent.initialResults.set(parent.initialResults.get());
			Boolean cont = true;
			while(cont){
				//System.out.println("Waiting... ");
//				Thread.sleep(10);

                
				try{
//					System.out.println(kmerQueue);
                	tuple.set(kmerQueue.poll(1, TimeUnit.MILLISECONDS));
                } catch (NullPointerException n) {
                	
                	return;
                }

				
                
				//System.out.println("Consumed " + gene.getString(2));
				
				if((KmerTuple)tuple.get() == null)
				{
//						System.out.println("Aligner done.");
					return;
				}
				
                
                kmer.set(tuple.get().kMer);
                
                i.set(tuple.get().i);


                testBacktrace.set(ConstructArray(target, kmer.get()));
                if (testBacktrace.get().length == 0 ){
                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
                    System.out.println();
        
                // For sequences of acceptable length
                } else {
                    //System.out.println("Splitter2.geneName= " + Splitter.geneName.get());
                    testResult.set(getResult(testBacktrace.get(), target.length(), kmer.get().length(), Splitter.geneName.get(), i.get()));
                	
                	if (Splitter.initialResults.get().size.get() == 0)
                	{
//                    	long startTime = System.nanoTime();
                		Splitter.initialResults.get().add(testResult.get()); 
//                        long endTime = System.nanoTime();
//                        System.out.println("Adding to initialResults took " + (endTime - startTime)/1000000 + " milliseconds");
                	}
                	else if(testResult.get().alignmentScore >= Splitter.initialResults.get().get(1).alignmentScore)
                	{
                		Splitter.initialResults.get().add(testResult.get());  
                	}
                    initialBestScore.set(Splitter.initialResults.get().get(1).alignmentScore);
////                	
//                    ThreadLocal<Integer> l = new ThreadLocal<Integer>();
                    
//                	long startTime = System.nanoTime();

                    ThreadLocal<Integer> l = new ThreadLocal<Integer>();

                	for(l.set(1); l.get() < Splitter.initialResults.get().size(); l.set(l.get()+1))
                	{
                		
            			if (Splitter.initialResults.get().get(l.get()).alignmentScore < initialBestScore.get()) 
                		{
                		
        					Splitter.initialResults.get().remove(Splitter.initialResults.get().get(l.get()));
        					l.set(l.get()-1);
                		}	

                	}
//                	long endTime = System.nanoTime();
//                    System.out.println("Removing from initialResults took " + (endTime - startTime)/1000000 + " milliseconds");
                	
//                	Splitter.initialResults.get().cleanUp();
                	         
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
		
//		long startTime = System.nanoTime();
		
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
			
//			ThreadLocal<int [][]> ourArray = new ThreadLocal<int[][]>();
//			ourArray.set(new int[sequence1.get().length + 1][sequence2.get().length +1]);
//			
//			ThreadLocal<int [][]> backtraceArray = new ThreadLocal<int[][]>();
//			backtraceArray.set(new int[sequence1.get().length + 1][sequence2.get().length +1]);
			
			int[][] ourArray = new int[sequence1.get().length + 1][sequence2.get().length +1];
			int[][] backtraceArray = new int[sequence1.get().length + 1][sequence2.get().length +1];
			
			int i =1;
			int j=1;
					
//			ThreadLocal<Integer> i = new ThreadLocal<Integer>();
//			i.set(1);
//			ThreadLocal<Integer> j = new ThreadLocal<Integer>();
//			j.set(1);
			
			for(i=1; i< sequence1.get().length + 1; i++){
				for(j=1; j < sequence2.get().length + 1; j++){
//					ThreadLocal<Integer> matchMax = new ThreadLocal<Integer>();
                    int matchMax = (Math.max(Math.max(ourArray[i-1][j], ourArray[i][j-1]), 1+ ourArray[i-1][j-1]));
//					ThreadLocal<Integer> mismatchMax = new ThreadLocal<Integer>();
                    int mismatchMax = (Math.max(ourArray[i-1][j], ourArray[i][j-1]));
					// MATCH
					if(sequence1.get()[i-1] == sequence2.get()[j-1]){
						ourArray[i][j] = matchMax;
						
						//BackTrace
						if (matchMax == 1+ ourArray[i-1][j-1]){
							// 3 = Trace from the diagonal
//							System.out.println("1st " + backtraceArray.get()[i.get()][j.get()]);

							backtraceArray[i][j] = 3;
//							System.out.println("2nd " + backtraceArray.get()[i.get()][j.get()]);

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
//			i.set(null);
//			j.set(null);
//			long endTime = System.nanoTime();
//            System.out.println("ConstructArray took " + (endTime - startTime)/1000000 + " milliseconds");
			return backtraceArray;
			
		} catch (OutOfMemoryError m) {
			int [][] emptyArray = new int[0][0];
			return emptyArray;
		}
	}
	
	public static AlignResultConcurrent getResult(int[][] backtrace, int i, int j, String gene, int iteration){

       // System.out.println("gene= " +gene);

//		ThreadLocal<Integer> i = new ThreadLocal<Integer>();
//		i.set(oldI);
//		ThreadLocal<Integer> j = new ThreadLocal<Integer>();
//		j.set(oldJ);
		
//		long startTime = System.nanoTime();
		
		//Initialize scoring
		int numMatches = 0;
		int numStartGaps = 0;
		int numContGaps = 0;
		
		ThreadLocal<CharLazyList> gap1 = new ThreadLocal<CharLazyList>();
		gap1.set(new CharLazyList());
		ThreadLocal<CharLazyList> gap2 = new ThreadLocal<CharLazyList>();
		gap2.set(new CharLazyList());
		
		
		while(backtrace[i][j] != 0){
			if(backtrace[i][j] == 3){
				// Mark traversed
				backtrace[i][j] = 6;
				
				// Increase match count
				numMatches++;
				
				// Create alignment
//				System.out.println("******************");
//				System.out.println(gap1.get());
//				System.out.println("******************");
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
					numContGaps++;
				} else {
					numStartGaps++;
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
					numContGaps++;
				} else {
					numStartGaps++;
				}
				
				// Create alignment
				gap1.get().add(sequence1.get()[i-1]);
				gap2.get().add('-');
				
				// Go to next cell
				i--;
			}
		}
		
		backtrace = null;

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
				j--;;
			}
		}
		
		int finalScore;
        finalScore = ((numMatches*matchScore) - (numStartGaps*startGapScore) - (numContGaps*continueGapScore));
        //System.out.println("gene1= " +gene);
        ThreadLocal<AlignResultConcurrent> result = new ThreadLocal<AlignResultConcurrent>();
        result.set(new AlignResultConcurrent(gene +" - "+ iteration, gap1.get(), gap2.get(), numMatches, finalScore));
        //System.out.println("gene2= " +gene);
        
//        gap1.remove();
//        gap2.remove();
        
//        finalScore.set(null);
//        i.set(null);
//		j.set(null);

//		System.out.println("FINAL SCORE = " + finalScore);
//		System.out.println("Aligned Sequence = " + gap1);
        
//        long endTime = System.nanoTime();
//        System.out.println("getResult took " + (endTime - startTime)/1000000 + " milliseconds");
//        
		return result.get();
	}
	
	
}

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Aligner implements Runnable {
	private BlockingQueue<KmerTuple> kmerQueue;// = new BlockingQueue<KmerTuple>();
	public String target;
    //private ThreadLocal<String> geneName;
    
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

	
	public Aligner(BlockingQueue<KmerTuple> q, String t){//, String name) {
		this.kmerQueue = q;
		this.target = t;

        //this.geneName = new ThreadLocal<String>();
        //this.geneName.set(name);
    }
	
	public void run() {
        //System.out.println("Splitter.geneName= " +Splitter.geneName.get());

		
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
				Thread.sleep(10);

                
				try{
//					System.out.println(kmerQueue);
                	tuple.set(kmerQueue.poll(500, TimeUnit.MILLISECONDS));
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
                
//					System.out.println("Consumed " + geneName+ " "+ i);
				
				
				

                testBacktrace.set(ConstructArray(target, kmer.get()));
                if (testBacktrace.get().length == 0 ){
                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
                    System.out.println();
        
                // For sequences of acceptable length
                } else {
                    //System.out.println("Splitter2.geneName= " + Splitter.geneName.get());
                    testResult.set(getResult(testBacktrace.get(), target.length(), kmer.get().length(), Splitter.geneName.get(), i.get()));
                	
                	lock.lock();
                	if (Splitter.initialResults.get().size() == 0)
                	{
                		Splitter.initialResults.get().add(testResult.get()); 
                	}
                	else if(testResult.get().alignmentScore >= Splitter.initialResults.get().get(1).alignmentScore)
                	{
                		Splitter.initialResults.get().add(testResult.get());  
                	}
                    initialBestScore.set(Splitter.initialResults.get().get(1).alignmentScore);
                	lock.unlock();
                	
                	lock.lock();
//                    ThreadLocal<Integer> l = new ThreadLocal<Integer>();
                    
					
                	for(int l=1; l < Splitter.initialResults.get().size(); l++)
                	{
            			if (Splitter.initialResults.get().get(l).alignmentScore < initialBestScore.get()) 
                		{
                		
        					Splitter.initialResults.get().remove(Splitter.initialResults.get().get(l));
        					l--;
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
			
			
			ThreadLocal<Integer> i = new ThreadLocal<Integer>();
			i.set(1);
			ThreadLocal<Integer> j = new ThreadLocal<Integer>();
			j.set(1);
			
			for(i.set(1); i.get() < sequence1.get().length + 1; i.set(i.get()+1)){
				for(j.set(1); j.get() < sequence2.get().length + 1; j.set(j.get()+1)){
					ThreadLocal<Integer> matchMax = new ThreadLocal<Integer>();
                    matchMax.set(Math.max(Math.max(ourArray.get()[i.get()-1][j.get()], ourArray.get()[i.get()][j.get()-1]), 1+ ourArray.get()[i.get()-1][j.get()-1]));
					ThreadLocal<Integer> mismatchMax = new ThreadLocal<Integer>();
                    mismatchMax.set(Math.max(ourArray.get()[i.get()-1][j.get()], ourArray.get()[i.get()][j.get()-1]));
					// MATCH
					if(sequence1.get()[i.get()-1] == sequence2.get()[j.get()-1]){
						ourArray.get()[i.get()][j.get()] = matchMax.get();
						
						//BackTrace
						if (matchMax.get() == 1+ ourArray.get()[i.get()-1][j.get()-1]){
							// 3 = Trace from the diagonal
//							System.out.println("1st " + backtraceArray.get()[i.get()][j.get()]);

							backtraceArray.get()[i.get()][j.get()] = 3;
//							System.out.println("2nd " + backtraceArray.get()[i.get()][j.get()]);

						} else if (matchMax.get() == ourArray.get()[i.get()][j.get()-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2
							backtraceArray.get()[i.get()][j.get()] = 2;
						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray.get()[i.get()][j.get()] = 1;
						}
						
					// MISMATCH
					} else {
						ourArray.get()[i.get()][j.get()] = mismatchMax.get();
						
						//BackTrace
						if (matchMax.get() == ourArray.get()[i.get()][j.get()-1]) {
							// 2 = Trace from the left
							// AKA Gap in Seq2

							backtraceArray.get()[i.get()][j.get()] = 2;

						} else {
							// 1 = Trace from the top
							// AKA Gap in Seq1
							backtraceArray.get()[i.get()][j.get()] = 1;
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
	
	public static AlignResultConcurrent getResult(int[][] backtrace, int oldI, int oldJ, String gene, int iteration){

       // System.out.println("gene= " +gene);

		ThreadLocal<Integer> i = new ThreadLocal<Integer>();
		i.set(oldI);
		ThreadLocal<Integer> j = new ThreadLocal<Integer>();
		j.set(oldJ);
		
		//Initialize scoring
		numMatches.set(0);
		numStartGaps.set(0);
		numContGaps.set(0);
		
		ThreadLocal<CharLazyList> gap1 = new ThreadLocal<CharLazyList>();
		gap1.set(new CharLazyList());
		ThreadLocal<CharLazyList> gap2 = new ThreadLocal<CharLazyList>();
		gap2.set(new CharLazyList());
		
		
		while(backtrace[i.get()][j.get()] != 0){
			if(backtrace[i.get()][j.get()] == 3){
				// Mark traversed
				backtrace[i.get()][j.get()] = 6;
				
				// Increase match count
				numMatches.set(numMatches.get()+1);
				
				// Create alignment
//				System.out.println("******************");
//				System.out.println(gap1.get());
//				System.out.println("******************");
				gap1.get().add(sequence1.get()[i.get()-1]);
				gap2.get().add(sequence2.get()[j.get()-1]);
				
				// Go to next cell
				i.set(i.get()-1);
				j.set(j.get()-1);
				
			} else if (backtrace[i.get()][j.get()] == 2) {
				// Mark traversed
				backtrace[i.get()][j.get()] = 5;
				
				// Increase gap score
				if(j.get() != sequence2.get().length && backtrace[i.get()][j.get()+1] == 5) {
					numContGaps.set(numContGaps.get()+1);
				} else {
					numStartGaps.set(numStartGaps.get()+1);
				}
				
				// Create alignment
				gap1.get().add('-');
				gap2.get().add(sequence2.get()[j.get()-1]);
				
				// Go to next cell
				j.set(j.get()-1);
				
			} else if (backtrace[i.get()][j.get()] == 1) {
				// Mark traversed
				backtrace[i.get()][j.get()] = 4;
				
				// Increase gap score
				if(i.get() != sequence1.get().length && backtrace[i.get()+1][j.get()] == 4){
					numContGaps.set(numContGaps.get()+1);
				} else {
					numStartGaps.set(numStartGaps.get()+1);
				}
				
				// Create alignment
				gap1.get().add(sequence1.get()[i.get()-1]);
				gap2.get().add('-');
				
				// Go to next cell
				i.set(i.get()-1);
			}
		}

		if (i.get()>0){
			while (i.get()>0){
				gap1.get().add(sequence1.get()[i.get()-1]);
				gap2.get().add('-');
				i.set(i.get()-1);
			}
		}
		
		if (j.get()>0){
			while (j.get()>0){
				gap1.get().add('-');
				gap2.get().add(sequence2.get()[j.get()-1]);
				j.set(j.get()-1);
			}
		}
		
		ThreadLocal<Integer> finalScore = new ThreadLocal<Integer>();
        finalScore.set((numMatches.get()*matchScore) - (numStartGaps.get()*startGapScore) - (numContGaps.get()*continueGapScore));
        //System.out.println("gene1= " +gene);
        ThreadLocal<AlignResultConcurrent> result = new ThreadLocal<AlignResultConcurrent>();
        result.set(new AlignResultConcurrent(gene +" - "+ iteration, gap1.get(), gap2.get(), numMatches.get(), finalScore.get()));
        //System.out.println("gene2= " +gene);

//		System.out.println("FINAL SCORE = " + finalScore);
//		System.out.println("Aligned Sequence = " + gap1);
		return result.get();
	}
	
	
}

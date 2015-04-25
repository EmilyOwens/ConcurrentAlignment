//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Aligner implements Runnable {
		private BlockingQueue<KmerTuple> kmerQueue;
		public String target;
        public String geneName;
        public Splitter parent;
		
		public Aligner(BlockingQueue<KmerTuple> q, String t, String name, Splitter parent) {
			this.kmerQueue = q;
			this.target = t;
            this.geneName = name;
            this.parent = parent;
        }
		
		public void run() {

			
			try 
			{
//				parent.initialResults.set(parent.initialResults.get());
				Boolean cont = true;
				while(cont){
					//System.out.println("Waiting... ");
					Thread.sleep(10);
					KmerTuple tuple = kmerQueue.poll(500, TimeUnit.MILLISECONDS);
					
                    
					//System.out.println("Consumed " + gene.getString(2));
					
					if(tuple == null)
					{
						System.out.println("Aligner done.");
						return;
					}
					
                    String kmer = tuple.kMer;
                    int i = tuple.i;
                    
					System.out.println("Consumed " + geneName+ " "+ i);
					
					
					
					int[][] testBacktrace = ConcurrentAlignment.ConstructArray(target, kmer);
		                if (testBacktrace.length == 0 ){
		                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
		                    System.out.println();
		        
		                // For sequences of acceptable length
		                } else {
		                	AlignResult testResult = ConcurrentAlignment.getResult(testBacktrace, target.length(), kmer.length(), geneName, i);
		                	
		                	if(testResult.alignmentScore >= Splitter.initialResults.get().get(1).alignmentScore)
		                	{
		                		Splitter.initialResults.get().add(testResult);  
		                	}
//		                	          

		                }
				}
				

			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
		}
	}

//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Aligner implements Runnable {
		private BlockingQueue<DbResult> kmerQueue;
		public String target;
        public String geneName;
		
		public Aligner(BlockingQueue<DbResult> q, String t, String name) {
			this.kmerQueue = q;
			this.target = t;
            this.geneName = name;
		}
		
		public void run() {

			
			try 
			{
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
					
                    String kmer = tuple.kmer;
                    int i = tuple.i;
                    
					System.out.println("Consumed " + geneName);
					
					
					
					int[][] testBacktrace = ConcurrentAlignment.ConstructArray(target, kmer);
		                if (testBacktrace.length == 0 ){
		                    System.out.println("Sequence length exceeded maximum. Alignment not computed.");
		                    System.out.println();
		        
		                // For sequences of acceptable length
		                } else {
		                	ConcurrentAlignment.testResult = ConcurrentAlignment.getResult(testBacktrace, ConcurrentAlignment.sequence1.length, ConcurrentAlignment.sequence2.get().length);
		                	ConcurrentAlignment.finalResults.add(ConcurrentAlignment.testResult);            
		                }
				}
				

			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
		}
	}

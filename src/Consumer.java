//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Consumer implements Runnable {
		private BlockingQueue<DbResult> queue1;
		public String target;

		
		public Consumer(BlockingQueue<DbResult> q, String t) {
			this.queue1 = q;
			this.target = t;
		}
		
		public void run() {

			
			try 
			{
				Boolean cont = true;
				while(cont){
					//System.out.println("Waiting... ");
					Thread.sleep(10);
					DbResult gene = queue1.poll(500, TimeUnit.MILLISECONDS);
					
					//System.out.println("Consumed " + gene.getString(2));
					
					if(gene == null)
					{
						System.out.println("Done.");
						return;
					}
					
					System.out.println("Consumed " + gene.geneName);
					
					String sequence = gene.sequence;
					
					
					int[][] testBacktrace = ConcurrentAlignment.ConstructArray(target, sequence);
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
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Splitter implements Runnable {
		private BlockingQueue<DbResult> queue1;
		public String target;
		
		public BlockingQueue<String> queue2;

		
		public Splitter(BlockingQueue<DbResult> q, String t) {
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
					
					
					String sequence = gene.sequence;
					String geneName = gene.geneName;
					
					queue2 = new LinkedBlockingQueue<String>(25);
					
					for (int i=0; i < (sequence.length() - target.length()); i++)
					{
//						System.out.println("Step " + i + " of " + (sequence.length() - target.length()));
	                	String kMer = sequence.substring(i, i+target.length());
	                	
	                	queue2.put(kMer);
					}
					
					
					
				
		                
		                System.out.println("Consumed " + gene.geneName);
				}
				
				
				

			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
		}
	}
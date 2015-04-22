//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class Splitter implements Runnable {
		private BlockingQueue<DbResult> queue1;
		public String target;
		
		public BlockingQueue<KmerTuple> queue2;
		
		public ThreadLocal<LinkedList<AlignResult>> initialResults = new ThreadLocal<LinkedList<AlignResult>>();

		
		public Splitter(BlockingQueue<DbResult> q, String t) {
			this.queue1 = q;
			this.target = t;
			this.initialResults.set(new LinkedList<AlignResult>());
		}
		
		public void run() {

			
			try 
			{
				Boolean cont = true;
				while(cont){
					//System.out.println("Waiting... ");
					Thread.sleep(10);
					DbResult gene = queue1.poll(500, TimeUnit.MILLISECONDS);
					
//					System.out.println("Consumed " + gene.getString(2));
					
					if(gene == null)
					{
						System.out.println("Done.");
						return;
					}
					
					
					String sequence = gene.sequence;
					String geneName = gene.geneName;
					
					queue2 = new LinkedBlockingQueue<KmerTuple>((sequence.length() - target.length()));
					
					for (int i=0; i < (sequence.length() - target.length()); i++)
					{
//						System.out.println("Step " + i + " of " + (sequence.length() - target.length()));
	                	String kMer = sequence.substring(i, i+target.length());
	                	
	                	KmerTuple ourKMer = new KmerTuple(kMer, i);
	                	
	                	queue2.put(ourKMer);
	                	
					}
		               System.out.println("Consumed " + gene.geneName);
		               
		               Aligner aligner = new Aligner(queue2, target, geneName, this);
						
		               Thread a1 = new Thread(aligner);
		               Thread a2 = new Thread(aligner);
		               
		               a1.start();
		               a2.start();
		               
		               try {
		            	   a1.join();
		            	   a2.join();
		               } catch (InterruptedException e){}
		               
				}
				
				
				
				
				

			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
		}
	}

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
//import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Splitter implements Runnable {
		private BlockingQueue<DbResult> queue1;
		public String target;
		
		public BlockingQueue<KmerTuple> queue2;
		
		public Lock printLock = new ReentrantLock();
		
		public static InheritableThreadLocal<CleanLazyList> initialResults = new InheritableThreadLocal<CleanLazyList>();
        public static InheritableThreadLocal<String> geneName = new InheritableThreadLocal<String>();
        public static ThreadLocal<String> sequence = new ThreadLocal<String>();
        public static Aligner aligner;
		
		public Splitter(BlockingQueue<DbResult> q, String t) {
			this.queue1 = q;
			this.target = t;
//			this.initialResults.set(new CleanLazyList());
			
		}
		
		public void run() {

			
			try 
			{
//				System.out.println(initialResults.get().add(new AlignResult("Test", new LinkedList<Character>(), new LinkedList<Character>(), 0, 7357)));
				Boolean cont = true;
				while(cont){
                    initialResults.set(new CleanLazyList());
					//System.out.println("Waiting... ");
					Thread.sleep(10);
					DbResult gene = queue1.poll(500, TimeUnit.MILLISECONDS);
					
//					System.out.println("Consumed " + gene.getString(2));
					
					if(gene == null)
					{
						System.out.println("Done.");
						return;
					}
					
					//System.out.println("gene.geneName= " +gene.geneName);
                    sequence.set(gene.sequence);
                    geneName.set(gene.geneName);
					//System.out.println("geneName= " +geneName.get());

					queue2 = new LinkedBlockingQueue<KmerTuple>(10*(sequence.get().length() - target.length()));
					
					for (int i=0; i < (sequence.get().length() - target.length()); i++)
					{
//						System.out.println("Step " + i + " of " + (sequence.length() - target.length()));
	                	String kMer = sequence.get().substring(i, i+target.length());
	                	
	                	KmerTuple ourKMer = new KmerTuple(kMer, i);
	                	
	                	queue2.put(ourKMer);
//	                	System.out.println("Got here");
	                	
					}
		               
	               aligner = new Aligner(queue2, target);//, geneName.get());
					
	               Thread a1 = new Thread(aligner);
	               Thread a2 = new Thread(aligner);
	               
	               a1.start();
	               a2.start();
	               
	               try {
	            	   a1.join();
	            	   a2.join();
	               } catch (InterruptedException e){}
                   
	               printLock.lock();
		            System.out.println("-----------------");
                    //System.out.println("geneName= " + geneName.get());
		            for (int l=1; l <=initialResults.get().size(); l++)
			           	{  
			            	   System.out.println(initialResults.get().get(l).geneName + " = " + initialResults.get().get(l).alignmentScore);
			           	}
		            System.out.println("-----------------");
		            printLock.unlock();
	               

	            
//		               System.out.println("Consumed " + gene.geneName);

		               
				}
				
			
			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
			
		}
	}

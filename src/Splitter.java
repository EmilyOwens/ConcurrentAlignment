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
        //public static Aligner aligner;
		
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
					ThreadLocal<DbResult> gene = new ThreadLocal<DbResult>();
                    gene.set(queue1.poll(500, TimeUnit.MILLISECONDS));
					
//					System.out.println("Consumed " + gene.getString(2));
					
					if(gene.get() == null)
					{
						System.out.println("Done.");
						return;
					}
					
					//System.out.println("gene.geneName= " +gene.geneName);
                    sequence.set(gene.get().sequence);
                    geneName.set(gene.get().geneName);
					//System.out.println("geneName= " +geneName.get());

					queue2 = new LinkedBlockingQueue<KmerTuple>(10*(sequence.get().length() - target.length()));
					ThreadLocal<Integer> i = new ThreadLocal<Integer>();
                    
					for (i.set(0); i.get() < (sequence.get().length() - target.length()); i.set(i.get()+1))
					{
//						System.out.println("Step " + i + " of " + (sequence.length() - target.length()));
	                	ThreadLocal<String> kMer = new ThreadLocal<String>();
                        kMer.set(sequence.get().substring(i.get(), i.get()+target.length()));
	                	
	                	ThreadLocal<KmerTuple> ourKMer = new ThreadLocal<KmerTuple>();
                        ourKMer.set(new KmerTuple(kMer.get(), i.get()));
	                	
	                	queue2.put(ourKMer.get());
//	                	System.out.println("Got here");
	                	
					}
		               
	               //aligner = new Aligner(queue2, target);//, geneName.get());
					
	               Thread a1 = new Thread(new Aligner(queue2, target));
	               Thread a2 = new Thread(new Aligner(queue2, target));
	               Thread a3 = new Thread(new Aligner(queue2, target));
	               Thread a4 = new Thread(new Aligner(queue2, target));
	               Thread a5 = new Thread(new Aligner(queue2, target));
	               Thread a6 = new Thread(new Aligner(queue2, target));
	               Thread a7 = new Thread(new Aligner(queue2, target));
	               Thread a8 = new Thread(new Aligner(queue2, target));
	               
	               a1.start();
	               a2.start();
	               a3.start();
	               a4.start();
	               a5.start();
	               a6.start();
	               a7.start();
	               a8.start();
	               
	               try {
	            	   a1.join();
	            	   a2.join();
	            	   a3.join();
	            	   a4.join();
	            	   a5.join();
	            	   a6.join();
	            	   a7.join();
	            	   a8.join();
	               } catch (InterruptedException e){}
                   
	               //printLock.lock();
		           // System.out.println("-----------------");
                   // //System.out.println("geneName= " + geneName.get());
		           // for (int l=1; l <=initialResults.get().size(); l++)
			       //    	{  
			       //     	   System.out.println(initialResults.get().get(l).geneName + " = " + initialResults.get().get(l).alignmentScore);
			       //    	}
		           // System.out.println("-----------------");
		           // printLock.unlock();
                   // 
                    ThreadLocal<Integer> j = new ThreadLocal<Integer>();
                    ThreadLocal<Integer> k = new ThreadLocal<Integer>();
                    
                    for (j.set(1); j.get() <= initialResults.get().size(); j.set(j.get()+1))
                    {
                        if (ConcurrentAlignment.finalResults.size() == 0)
                        {
                            ConcurrentAlignment.finalResults.add(initialResults.get().get(j.get()));
                        } else {
                            ThreadLocal<Integer> finalBestScore = new ThreadLocal<Integer>();
                            finalBestScore.set(ConcurrentAlignment.finalResults.get(1).alignmentScore);
                            if (initialResults.get().get(j.get()).alignmentScore >= finalBestScore.get()-2)
                            {
                                ConcurrentAlignment.finalResults.add(initialResults.get().get(j.get()));
                            }
                        }
                    }
            	
//                    initialResults.set(null);
                    
                    ThreadLocal<Integer> finalBestScore = new ThreadLocal<Integer>();
                    finalBestScore.set(ConcurrentAlignment.finalResults.get(1).alignmentScore);
                    
                    for (k.set(1); k.get() <= ConcurrentAlignment.finalResults.size(); k.set(k.get()+1))
                    {
                        if (ConcurrentAlignment.finalResults.get(k.get()).alignmentScore < finalBestScore.get()-2)
                        {
                            ConcurrentAlignment.finalResults.remove(ConcurrentAlignment.finalResults.get(k.get()));
                            k.set(k.get()-1);
                        }
                    }
	               
               
	            
//		               System.out.println("Consumed " + gene.geneName);

		               
				}
				
			
			} catch(InterruptedException e) { }//e.printStackTrace(); }
			
			
			
			
		}
	}

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.locks.*;

public class Producer implements Runnable {
		private BlockingQueue<DbResult> queue1;
		private ResultSet results;
		
		public  ResultSet Done = null;
		
		public Producer(BlockingQueue<DbResult> q, ResultSet r) {
			this.queue1 = q;
			this.results = r;
		}
		
		public static DbResult[] producerArray = new DbResult[101];
		
		@Override
		public void run() {
			long startTime = System.nanoTime();
			try{
				ConcurrentAlignment.lock.lock();
	            Boolean next = results.next();
	            ConcurrentAlignment.lock.unlock();
	            int index = 0;
	            while(next && results.getInt(1) < 25){
//	            
	                try {
	                	Thread.sleep(5);
//	                	ConcurrentAlignment.lock.lock();
	                	DbResult ourResult = new DbResult(results.getInt(1), results.getString(2), results.getString(3));
	                	producerArray[index] = ourResult;
//	                	System.out.println(producerArray[0].getString(2));
	                	System.out.println("Start Produce "+results.getString(2));
//	                	queue1.put(results);
//	                	System.out.println("End Produce "+results.getString(2));
	                } catch (InterruptedException e) { }
	            	
//	                System.out.println("Did Produce "+results.getString(2));
//	                ConcurrentAlignment.lock.lock();
	                next = results.next();
	                //ConcurrentAlignment.lock.unlock();
	                //System.out.println("Will Produce "+results.getString(2));
	                index++;
	            }

	            	
	            for (int i=0; i<25; i++)
	            {
	            	try {
	            		//System.out.println("Should Produce "+producerArray[i].geneName);
//	            		System.out.println("Produced "+producerArray[i].geneName);
	            		queue1.put(producerArray[i]);
	            		
	            	} catch (InterruptedException | NullPointerException e) {
//						e.printStackTrace();
    				}
	            }

	            
	        }
	        catch (SQLException sqlExcept){
	            sqlExcept.printStackTrace();
	        }
			
			long endTime = System.nanoTime();
	        System.out.println("Producer took " + (endTime - startTime)/1000000 + " milliseconds");
			
		}
		
	}
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.locks.*;

public class Producer implements Runnable {
		private BlockingQueue<ResultSet> queue1;
		private ResultSet results;
		
		public  ResultSet Done = null;
		
		public Producer(BlockingQueue<ResultSet> q, ResultSet r) {
			this.queue1 = q;
			this.results = r;
		}
		
		@Override
		public void run() {
			try{
				ConcurrentAlignment.lock.lock();
	            Boolean next = results.next();
	            ConcurrentAlignment.lock.unlock();
	            
	            while(next){
//	                
	                try {
	                	Thread.sleep(5);
	                	queue1.put(results);
	                	System.out.println("Produced "+results);
	                } catch (InterruptedException e) { }
	            	
	                ConcurrentAlignment.lock.lock();
	                next = results.next();
	                ConcurrentAlignment.lock.unlock();
	            }
	            
	            // Note that there won't be anything else added to queue
	           
	            try {
					queue1.put(null);
				} catch (InterruptedException | NullPointerException e) {
					//e.printStackTrace();
				}

	            
	        }
	        catch (SQLException sqlExcept){
	            sqlExcept.printStackTrace();
	        }
		}
		
	}
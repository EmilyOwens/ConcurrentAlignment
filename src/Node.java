import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Node {
      
      public int key;
      public Node next;
      public boolean marked;
      private Lock nodeLock = new ReentrantLock();
    
      public AlignResultConcurrent data;
      
      
      public Node(int item, AlignResultConcurrent item2) {
         this.key = item;
         this.marked = false;
         this.data = item2;
//         this.next = null;
//         this.nodeLock = new ReentrantLock();
         
      }
          
      public AlignResultConcurrent getData() {
    	  return data;
      }

      public void lock() {
    	  	nodeLock.lock();
	  }
	    
      public void unlock() {  
    	  nodeLock.unlock();  
      }

}

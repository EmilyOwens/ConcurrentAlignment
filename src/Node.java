import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Node {
      
      public int key;
      public Node next;
      public boolean marked;
      private Lock nodeLock = new ReentrantLock();
    
      public AlignResult data;
      
      
      public Node(int item, AlignResult data) {
         this.key = item;
         this.marked = false;
         this.data = data;
         
      }
          
      public AlignResult getData() {
    	  return data;
      }

      public void lock() {
    	  	nodeLock.lock();
	  }
	    
      public void unlock() {  
    	  nodeLock.unlock();  
      }

}

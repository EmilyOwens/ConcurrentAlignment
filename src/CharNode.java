import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class CharNode {
      
      public char key;
      public CharNode next;
      public boolean marked;
      private Lock nodeLock = new ReentrantLock();
    
      
      
      public CharNode(char item) {
         this.key = item;
         this.marked = false;
         
      }
          
    

      public void lock() {
    	  	nodeLock.lock();
	  }
	    
      public void unlock() {  
    	  nodeLock.unlock();  
      }

}

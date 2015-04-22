import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Node<T> {
      
      public int key;
      public Node<T> next;
      public boolean marked;
      private Lock nodeLock = new ReentrantLock();
    
      public Node(T item) {
         this.key = item.hashCode();
         this.marked = false;
         
      }
          

    public void lock() {
        nodeLock.lock();
    }
    
    public void unlock() {
        nodeLock.unlock();
    }

}

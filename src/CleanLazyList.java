import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CleanLazyList<T> {
	private Node head;
	public CleanLazyList() {
		head = new Node(Integer.MIN_VALUE);
		head.next = new Node(Integer.MAX_VALUE);
	}
	
	public boolean validate(Node pred, Node curr){
		return !pred.marked && !curr.marked && pred.next == curr;
	}
	
	public boolean add(T item) {
		int key = item.hashCode();
		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.key < key){
				pred = curr;
				curr = curr.next;
			}
			pred.lock();
			try {
				curr.lock();
				try {
					if (validate(pred, curr)){
						/*if (curr.key == key){
							return false;
						} else {*/
							Node node = new Node(item);
							node.next = curr;
							pred.next = node;
							return true;
						//}
					}
				} finally {
					curr.unlock();
				}
			} finally {
				pred.unlock();
			}
		}
	}
	
	public boolean remove(T item) {
		int key = item.hashCode();
		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.key < key){
				pred = curr;
				curr = curr.next;
			}
			pred.lock();
			
			
			try {
				curr.lock();
				try {
					if (validate(pred, curr)){
						if (curr.key != key){
							return false;
						} else {
							curr.marked = true;
							//pred.next = curr.next;
							return true;
						}
					}
				} finally {
					curr.unlock();
				}
			} finally {
				pred.unlock();
			}
		}
                
	}
	
	public boolean contains(T item) {
		int key = item.hashCode();
		Node curr = head;
		while (curr.key < key){
			curr = curr.next;
		}
		return curr.key == key && !curr.marked;
	}
	
	public boolean cleanUp() {
		Node pred = head;
		Node curr = pred.next;
		
		while (curr.key != Integer.MAX_VALUE){
			
			if (curr.marked == true) {
				pred.next = curr.next;
			}
			
			pred = curr;
			curr = curr.next;
		}
		
		return true;
	}
	
	public void print(){
                Node pred, curr;
                pred = head;
                curr = pred.next;
                while (curr.key!=Integer.MAX_VALUE){
                    pred = curr;
                    curr = curr.next;
                    System.out.println(pred.key);
                }
        
    }
	
	
}
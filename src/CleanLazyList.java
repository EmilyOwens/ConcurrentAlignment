//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;

public class CleanLazyList {
	private Node head;
    private int size;
    
	public CleanLazyList() {
		head = new Node(Integer.MAX_VALUE, null);
		head.next = new Node(Integer.MIN_VALUE, null);
        size = 0;
	}
	
	public boolean validate(Node pred, Node curr){
		return !pred.marked && !curr.marked && pred.next == curr;
	}
	

	public void add(AlignResultConcurrent item) {
		int key = item.alignmentScore;

		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.key > key){
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
							Node node = new Node(item.alignmentScore, item);
							node.next = curr;
							pred.next = node;
                            size++;
							return;
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
	

	public void remove(AlignResultConcurrent item) {
		int key = item.alignmentScore;

		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.key > key){
				pred = curr;
				curr = curr.next;
			}
			pred.lock();
			
			
			try {
				curr.lock();
				try {
					if (validate(pred, curr)){
						if (curr.key != key){
							return;
						} else {
							curr.marked = true;
                            size--;
							//pred.next = curr.next;
							return;
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
	
	public boolean contains(AlignResult item) {
		int key = item.hashCode();
		Node curr = head;
		while (curr.key > key){
			curr = curr.next;
		}
		return curr.key == key && !curr.marked;
	}
	
	public AlignResultConcurrent get(int index) {
		if (index <=0)
		{
			return null;
		}
		int i = 1;
		
		while(true) {
			Node pred = head;
			Node curr = head.next;
			
			while(i < index)
			{
				pred = curr;
				curr = curr.next;
				i++;
			}
			
			pred.lock();
			try {
				curr.lock();
				try {
					if (validate(pred, curr)){
	
							return curr.data;

					}
				} finally {
					curr.unlock();
				}
			} finally {
				pred.unlock();
			}
		}
		
	}
    
    public int size(){
        return size;
        }
	
	public boolean cleanUp() {
		Node pred = head;
		Node curr = pred.next;
		
		while (curr.key != Integer.MIN_VALUE){
			
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

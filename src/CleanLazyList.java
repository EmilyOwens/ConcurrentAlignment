import java.util.concurrent.atomic.AtomicInteger;

//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;

public class CleanLazyList {
	private Node head;
    public AtomicInteger size = new AtomicInteger(0);
    
    public ThreadLocal<Integer> topScore = new ThreadLocal<Integer>();
    
    
	public CleanLazyList() {
		AlignResultConcurrent junkHead = new AlignResultConcurrent("HEAD");
		AlignResultConcurrent junkTail = new AlignResultConcurrent("TAIL");
		head = new Node(Integer.MAX_VALUE, junkHead);
		head.next = new Node(Integer.MIN_VALUE, junkTail);
	}
	
	public boolean validate(Node pred, Node curr){
		return !pred.marked && !curr.marked && pred.next == curr;
	}
	

	public boolean add(AlignResultConcurrent item) {
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
                            size.getAndIncrement();
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
	

	public boolean remove(AlignResultConcurrent item) {
		int key = item.alignmentScore;

		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.key > key && curr.next != null){
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
                            size.getAndDecrement();
							pred.next = curr.next;
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
			AlignResultConcurrent junk = new AlignResultConcurrent("NOTHING");
			return junk;
		}
		AtomicInteger i = new AtomicInteger(1);
		
		while(true) {
			Node pred = head;
			Node curr = head.next;
			
			while(i.get() < index && curr.next != null)
			{
				pred = curr;
				curr = curr.next;
				i.getAndIncrement();
//				System.out.println(i.get());
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
        return size.get();
        }
	
	public boolean cleanUp() {
		Node pred = head;
		Node curr = pred.next;
		
		while (curr.key != Integer.MIN_VALUE){
			
			if (curr.marked == true) {
				pred.lock();
				try {
					curr.lock();
					try{
						curr.next.lock();
						try {
							pred.next = curr.next;
						} finally {curr.next.unlock();}
					}finally {curr.unlock();}
				}finally {pred.unlock();}
					
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

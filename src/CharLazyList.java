//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;

public class CharLazyList {
	private CharNode head;
    private int size;
    
	public CharLazyList() {
		head = new CharNode(' ');
		head.next = new CharNode(' ');
        size = 0;
	}
	
	public boolean validate(CharNode pred, CharNode curr){
		return !pred.marked && !curr.marked && pred.next == curr;
	}
	

	public void add(char item) {

		while (true) {
			CharNode pred = head;
			CharNode curr = head.next;
			
			pred.lock();
			try {
				curr.lock();
				try {
					if (validate(pred, curr)){
						/*if (curr.key == key){
							return false;
						} else {*/
							CharNode node = new CharNode(item);
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
	

	/*public void remove(AlignResult item) {
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
	
	public AlignResult get(int index) {
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
    */
    public int size(){
        return size;
        }
	/*
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
	*/
	public void print(){
                CharNode pred, curr;
                pred = head;
                curr = pred.next;
                while (curr.key!=' '){
                    pred = curr;
                    curr = curr.next;
                    System.out.println(pred.key);
                }
        
    }
	
	
}

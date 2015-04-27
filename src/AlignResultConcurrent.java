
public class AlignResultConcurrent {
	String geneName;
	
	CharLazyList seq1;
	CharLazyList seq2;
	
	int numNtMatches;
	int alignmentScore;
	
	public AlignResultConcurrent(String geneName, CharLazyList seq1, CharLazyList seq2, int numNtMatches, int alignmentScore){
		this.geneName = geneName;
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.numNtMatches = numNtMatches;
		this.alignmentScore = alignmentScore;
	}
	
	public AlignResultConcurrent(String nodeName) {
		if (nodeName == "HEAD")
		{
			this.geneName = "NOTHING";
			this.seq1 = new CharLazyList();
			this.seq2 = new CharLazyList();
			this.numNtMatches = Integer.MIN_VALUE;
			this.numNtMatches = Integer.MIN_VALUE;
		} else if (nodeName == "TAIL") {
			
			this.geneName = "NOTHING";
			this.seq1 = new CharLazyList();
			this.seq2 = new CharLazyList();
			this.numNtMatches = Integer.MIN_VALUE;
			this.numNtMatches = Integer.MIN_VALUE;
			
		} else {
			this.geneName = "NOTHING";
			this.seq1 = new CharLazyList();
			this.seq2 = new CharLazyList();
			this.numNtMatches = Integer.MIN_VALUE;
			this.numNtMatches = Integer.MIN_VALUE;
		}

	}
	

	
}
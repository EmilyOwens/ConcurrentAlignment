
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
	
	public AlignResultConcurrent() {
		this.alignmentScore = Integer.MIN_VALUE;
	}
	
}
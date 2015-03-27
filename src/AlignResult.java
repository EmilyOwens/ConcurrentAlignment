import java.util.*;

public class AlignResult {
	ArrayList<Character> seq1;
	ArrayList<Character> seq2;
	
	int numNtMatches;
	int alignmentScore;
	
	public AlignResult(ArrayList<Character> seq1, ArrayList<Character> seq2, int numNtMatches, int alignmentScore){
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.numNtMatches = numNtMatches;
		this.alignmentScore = alignmentScore;
	}
	
}
import java.util.*;

public class AlignResult {
	String geneName;
	
	ArrayList<Character> seq1;
	ArrayList<Character> seq2;
	
	int numNtMatches;
	int alignmentScore;
	
	public AlignResult(String geneName, ArrayList<Character> seq1, ArrayList<Character> seq2, int numNtMatches, int alignmentScore){
		this.geneName = geneName;
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.numNtMatches = numNtMatches;
		this.alignmentScore = alignmentScore;
	}
	
}
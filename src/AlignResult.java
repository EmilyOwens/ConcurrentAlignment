import java.util.*;

public class AlignResult {
	ArrayList seq1;
	ArrayList seq2;
	
	int alignmentScore;
	
	public AlignResult(ArrayList seq1, ArrayList seq2, int alignmentScore){
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.alignmentScore = alignmentScore;
	}
	
}
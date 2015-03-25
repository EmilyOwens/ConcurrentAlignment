import java.util.*;

public class SequentialAlignment {
	
	public SequentialAlignment() {
		
	}
	
	private static char[] sequence1;
	private static char[] sequence2;
	
	public static char[] alignment1;
	public static char[] alignment2;
	
	public static ArrayList<Character> gappedSeq1 = new ArrayList<Character>();
	public static ArrayList<Character> gappedSeq2 = new ArrayList<Character>();
	
	
	public static int[][] InitArrays(String seq1, String seq2) {
		
		sequence1 = seq1.toCharArray();
		sequence2 = seq2.toCharArray();
		
		int[][] initialArray = new int[sequence1.length + 1][sequence2.length +1];
		
		return initialArray;
	}
	
	
	public static int[][] ConstructArray(String seq1, String seq2) {
		/*		
		 * 			0	S	e	q	2
		 * 			-	-	-	-	-
		 * 	 0	| 	0	0	0	0	0
		 * 	 S	| 	0	x	x	x	x
		 * 	 e	| 	0	x	x	x	x
		 * 	 q	| 	0	x	x	x	x
		 * 	 1	| 	0	x	x	x	x
		 * 
		 */
		int [][] ourArray = InitArrays(seq1,seq2);
		int [][] backtraceArray = InitArrays(seq1,seq2);
		
		for(int i=1; i < sequence1.length + 1; i++)
		{
			for(int j=1; j < sequence2.length + 1; j++)
			{
				int matchMax = Math.max(Math.max(ourArray[i-1][j], ourArray[i][j-1]), 1+ ourArray[i-1][j-1]);
				int mismatchMax = Math.max(ourArray[i-1][j], ourArray[i][j-1]);
				// MATCH
				if(sequence1[i-1] == sequence2[j-1])
				{
					ourArray[i][j] = matchMax;
					
					//BackTrace
					if (matchMax == 1+ ourArray[i-1][j-1])
					{
						// 3 = Trace from the diagonal
						backtraceArray[i][j] = 3;
					} else if (matchMax == ourArray[i][j-1]) {
						// 2 = Trace from the left
						// AKA Gap in Seq2
						backtraceArray[i][j] = 2;
					} else {
						// 1 = Trace from the top
						// AKA Gap in Seq1
						backtraceArray[i][j] = 1;
					}
					
				// MISMATCH
				} else {
					ourArray[i][j] = mismatchMax;
					
					//BackTrace
					if (matchMax == ourArray[i][j-1]) {
						// 2 = Trace from the left
						// AKA Gap in Seq2
						backtraceArray[i][j] = 2;
					} else {
						// 1 = Trace from the top
						// AKA Gap in Seq1
						backtraceArray[i][j] = 1;
					}
				}
				
				
			}
		}
		return backtraceArray;

	}
	
	public static AlignResult getResult(int[][] backtrace, int i, int j, ArrayList<Character> gap1, ArrayList<Character> gap2){
		
		//System.out.println(i);
		//System.out.println(sequence1[i-1]);
		
		if(backtrace[i][j] == 3)
		{
			gap1.add(0, sequence1[i-1]);
			gap2.add(0, sequence2[j-1]);
			return getResult(backtrace, i-1, j-1, gap1, gap2);
		} else if (backtrace[i][j] == 2) {
			gap1.add(0, '-');
			gap2.add(0, sequence2[j-1]);
			return getResult(backtrace, i, j-1, gap1, gap2);			
		} else if (backtrace[i][j] == 1) {
			gap1.add(0, sequence1[i-1]);
			gap2.add(0, '-');
			return getResult(backtrace, i-1, j, gap1, gap2);
		}
		
		if (i>0){
			while (i>0)
			{
				gap1.add(0, sequence1[i-1]);
				gap2.add(0, '-');
				i--;
			}
		}
		
		if (j>0){
			while (j>0)
			{
				gap1.add(0, '-');
				gap2.add(0, sequence2[j-1]);
				j--;
			}
		}

		AlignResult result = new AlignResult(gap1, gap2, 0);
		return result;
	}
	
	
	
	public static void main(String[] args){
		String test1 = "abcadaccdabb";
		String test2 = "abcdaacbad";
		
		int[][] testBacktrace = ConstructArray(test1, test2);
		
		AlignResult testResult = getResult(testBacktrace, sequence1.length, sequence2.length, gappedSeq1, gappedSeq2);
		
		//testArray[1][2] = 5;
		
		for(int i=0; i < sequence1.length + 1; i++)
		{
			for(int j=0; j < sequence2.length + 1; j++)
			{
				System.out.print(testBacktrace[i][j] + " ");
			}
			System.out.println();
		}
		
		//System.out.print(testBacktrace[sequence1.length][sequence2.length]);
		System.out.println();
		
		for(int i=0; i < testResult.seq1.size(); i++){
			System.out.print(testResult.seq1.get(i));
		}
		System.out.println();
		for(int i=0; i < testResult.seq2.size(); i++){
			System.out.print(testResult.seq2.get(i));
		}
		
		
	}
	
	
}
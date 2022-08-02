package mycode;

import java.io.*;
import java.util.*;


/**
 * Entry point for the string alignment project.
 * @author Mark Steinbruck
 *
 */
public class salign {
	public static void main(String[] args) throws IOException {
		
		/*
		 * The following block is extracting command-line arguments & variable setups. 
		 */
		String input_file = args[0]; // The file used as input for the program.
		String method = args[1]; // Denotes whether we are using global or fitting alignment for these two strings.
		String mismatch_penalty = args[2]; // Denotes the number representing the penalty for two mismatched characters.
		String gap_penalty = args[3]; // Denotes the number representing the penalty for a gap between the two strings.
		Integer mismatch = Integer.parseInt(mismatch_penalty); 
		Integer gap = Integer.parseInt(gap_penalty);
		String output_file = args[4];
		String problem_name = "";
		String x = "";
		String y = "";
		String[] result = new String[2];
		
		/*
		 * The following block of code is used to count the total number of "commands" in the input file.
		 * (Each command takes up 3 lines in the file.)
		 */
		BufferedReader brtest = new BufferedReader(new FileReader(input_file));
		int lines = 0;
		while ((brtest.readLine()) != null) {
			lines++;
		}
		lines = lines/3;
		brtest.close();
		
		/*
		 * This block of code parses each command given in the input file, and executes either the global, or fitting method written
		 * below. Then, the result of each method is added to the output file. 
		 */
		int c = 0;
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		FileWriter m = new FileWriter(output_file);
		while ((problem_name=br.readLine())!=null) {
			c++;
				x = br.readLine();
				y = br.readLine();
				if (method.equals("global")) {
					result = global(x, y, gap, mismatch);
					m.write(problem_name + "\n");
					m.write(x + "\n");
					m.write(y + "\n");
					m.write("-" + result[0] + "\t" + 0 + "\t" + y.length() + "\t" + result[1]);
					if (c < lines) {
						m.write("\n");
					}
				} else {
					result = fitting(x, y, gap, mismatch);
					m.write(problem_name + "\n");
					m.write(x + "\n");
					m.write(y + "\n");
					m.write("-" + result[0] + "\t" + result[2] + "\t" + result[3] + "\t" + result[1]);
					if (c < lines) {
						m.write("\n");
					}
				}
		}
		br.close();
		m.close();
	}
	
	/*
	 * This method is to handle global alignment between two strings. String x is the query, and String y is the reference. 
	 * Integer gap and mismatch represent the penalty given for each gap or mismatch in the optimal solution.
	 */
	public static String[] global(String x, String y, Integer gap, Integer mismatch) {
		int match = 0;
		int gap_i = 0;
		int gap_j = 0;
		int minimum = 0;
		String[] results = new String[2];
		int[][] opt = new int[y.length()+1][x.length()+1];
		int [][] backtrace = new int[y.length()+1][x.length()+1];
		opt[0][0] = 0;
		backtrace[0][0] = -1;
		
		for (int i = 1; i < y.length()+1; i++) {
			opt[i][0] = i*gap;
			backtrace[i][0] = 1;
		}
		for (int j = 1; j < x.length()+1; j++) {
			opt[0][j] = j*gap;
			backtrace[0][j] = 2; 
		}

		for (int i = 1; i < y.length()+1; i++) {
			for (int j = 1; j < x.length()+1; j++) {
				// Check min of (cost(a[i],b[j])+A[i-1,j-1]), (gap + A[i-1,j]), (gap + A[i, j-1])
				match = cost(x.charAt(j-1), y.charAt(i-1), mismatch)+opt[i-1][j-1];
				gap_i = gap + opt[i-1][j];
				gap_j = gap + opt[i][j-1];
				minimum = Math.min(Math.min(match, gap_i), gap_j);
				opt[i][j] = minimum;
				if (minimum == match) {
					backtrace[i][j] = 0;
				} else if (minimum == gap_i) {
					backtrace[i][j] = 1;
				} else if (minimum == gap_j) {
					backtrace[i][j] = 2;
				}
			}
		}
		
		StringBuilder new_x = new StringBuilder();
		StringBuilder new_y = new StringBuilder();

		// Backtrace
		int n = x.length();
		int m = y.length();
		while (backtrace[m][n] != -1) {
			// Diagonal, add both
			if (backtrace[m][n] == 0) {
				new_x.append(x.charAt(n-1));
				new_y.append((y.charAt(m-1)));
				m--;
				n--;
			} else if (backtrace[m][n] == 1) {
				new_y.append(y.charAt(m-1));
				new_x.append('-');
				m--;
			} else if (backtrace[m][n] == 2) {
				new_y.append('-');
				new_x.append(x.charAt(n-1));
				n--;
			}
		}
		
		new_x = new_x.reverse();
		new_y = new_y.reverse();

		// Now assmemble the cigar string via the modified previous strings
		StringBuilder cigar = new StringBuilder();
		int count = 0;
		char indicator;
		for (int i = 0; i < new_x.length(); i++) {
			count = 0;
			if (new_x.charAt(i)==new_y.charAt(i)) {
				indicator = '=';
				while (i < new_x.length() && new_x.charAt(i)==new_y.charAt(i)) {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else if (new_x.charAt(i)=='-') {
				indicator = 'D';
				while (i < new_x.length() && new_x.charAt(i)=='-') {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else if (new_y.charAt(i)=='-') {
				indicator = 'I';
				while (i < new_x.length() && new_y.charAt(i)=='-') {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else {
				indicator = 'X';
				while (i < new_x.length() && new_y.charAt(i)!='-'&&new_x.charAt(i)!='-'&&
						new_y.charAt(i) != new_x.charAt(i)) {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			}
		}

		Integer score = (opt[y.length()][x.length()]);
		String strScore = score.toString();
		results[0] = strScore;
		results[1] = cigar.toString();
		return results;
	}
	
	/*
	 * This method is to handle fitting alignment between two strings. String x is the query, and String y is the reference. 
	 * Integer gap and mismatch represent the penalty given for each gap or mismatch in the optimal solution.
	 */
	public static String[] fitting(String x, String y, Integer gap, Integer mismatch) {
		int match = 0;
		int gap_i = 0;
		int gap_j = 0;
		int minimum = 0;
		String[] results = new String[4];
		int[][] opt = new int[x.length()+1][y.length()+1];
		int [][] backtrace = new int[x.length()+1][y.length()+1];
		opt[0][0] = 0;
		backtrace[0][0] = -1;
		
		for (int i = 1; i < x.length()+1; i++) {
			opt[i][0] = i*gap;
			backtrace[i][0] = 1;
		}
		for (int j = 1; j < y.length()+1; j++) {
			opt[0][j] = 0;
			backtrace[0][j] = -1; 
		}
		
		for (int i = 1; i < x.length()+1; i++) {
			for (int j = 1; j < y.length()+1; j++) {
				// Check min of (cost(a[i],b[j])+A[i-1,j-1]), (gap + A[i-1,j]), (gap + A[i, j-1])
				match = cost(x.charAt(i-1), y.charAt(j-1), mismatch)+opt[i-1][j-1];
				gap_i = gap + opt[i-1][j];
				gap_j = gap + opt[i][j-1];
				minimum = Math.min(Math.min(match, gap_i), gap_j);
				opt[i][j] = minimum;
				if (minimum == match) {
					backtrace[i][j] = 0;
				} else if (minimum == gap_i) {
					backtrace[i][j] = 1;
				} else if (minimum == gap_j) {
					backtrace[i][j] = 2;
				}
			}
		}
		
		int min = opt[x.length()][0];
		
		int m = x.length();
		int n = 0;
		for (int i = 0; i < y.length()+1; i++) {
			if (opt[x.length()][i] < min) {
				min = opt[x.length()][i];
				n = i;
			}
		}
		
		Integer score = (min);
		String strScore = score.toString();
		
		StringBuilder new_x = new StringBuilder();
		StringBuilder new_y = new StringBuilder();
		
		Integer end_n = n;;
		String end_n_string = end_n.toString();
		
		while (backtrace[m][n] != -1) {
			
			// Diagonal, add both
			if (backtrace[m][n] == 0) {
				new_x.append(x.charAt(m-1));
				new_y.append((y.charAt(n-1)));
				m--;
				n--;
			} else if (backtrace[m][n] == 1) {
				new_y.append(x.charAt(m-1));
				new_x.append('-');
				m--;
			} else if (backtrace[m][n] == 2) {
				new_y.append('-');
				new_x.append(y.charAt(n-1));
				n--;
			}
		}
		
		new_x = new_x.reverse();
		new_y = new_y.reverse();
		
		StringBuilder cigar = new StringBuilder();
		int count = 0;
		char indicator;
		for (int i = 0; i < new_x.length(); i++) {
			count = 0;
			if (new_x.charAt(i)==new_y.charAt(i)) {
				indicator = '=';
				while (i < new_x.length() && new_x.charAt(i)==new_y.charAt(i)) {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else if (new_x.charAt(i)=='-') {
				indicator = 'I';
				while (i < new_x.length() && new_x.charAt(i)=='-') {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else if (new_y.charAt(i)=='-') {
				indicator = 'D';
				while (i < new_x.length() && new_y.charAt(i)=='-') {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			} else {
				indicator = 'X';
				while (i < new_x.length() && new_y.charAt(i)!='-'&&new_x.charAt(i)!='-'&&
						new_y.charAt(i) != new_x.charAt(i)) {
					count++;
					i++;
				}
				i--;
				cigar.append(count+""+indicator);
			}
		}

		Integer new_n = n;
		String string_n = new_n.toString();
		
		results[0] = strScore;
		results[1] = cigar.toString();
		results[2] = string_n;
		results[3] = end_n_string;
		return results; 
	}
	
	public static int cost(char x, char y, Integer m) {
		if (x == y) {
			return 0;
		} else {
			return m;
		}
	}
	
	public static  void printArr(int[][] in, String x, String y) {
		for (int i = 0; i < x.length()+1; i++) {
			for (int j = 0; j < y.length()+1; j++) {
				System.out.print(in[i][j] + " ");
			}
			System.out.println();
		}
	}
}

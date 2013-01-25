package pokerbots.player;

import java.util.*;

public class HelperUtils {
	//Generate permutations
	//perm[i][j] is j'th term of i'th permutation
	//n is size of array
	public static ArrayList<int[]> getPerm(int[] a) {
		int num = 1;
		for(int i = 2; i <= a.length; i++) 
			num *= i;
		ArrayList<int[]> perm = new ArrayList<int[]>(num+1);
		permute(a, a.length, perm);
		return perm;
	}
    private static void permute(int[] a, int n, ArrayList<int[]> perm) {
        if (n == 1) {
           perm.add(Arrays.copyOf(a, a.length));
           return;
        }
        for (int i = 0; i < n; i++) {
            swap(a, i, n-1);
            permute(a, n-1, perm);
            swap(a, i, n-1);
        }
    }  
    private static void swap(int[] a, int i, int j) {
        int c;
        c = a[i]; a[i] = a[j]; a[j] = c;
    }

    //linear interpolation of val from range inLo inHi to range outLo outHi
    public static int linInterp(double val, double inLo, double inHi, double outLo, double outHi) {
    	val -= inLo;
    	inHi -= inLo;
    	outHi -= outLo;
    	
    	double ratio = val / inHi;
    	
    	return (int)(outLo + ratio * outHi);
    }
    
    //return min if val < min and max if val > max
    public static int minMax(double val, int min, int max) {
    	return (int)Math.max(Math.min(val, max), min);
    }
    
    public static double logistic(double max1, double max2, double input) {
    	max1 /= 100;
    	max2 /= 100;
    	input /= 100;
    	double c = 1.0 / Math.exp(-max1 / 2);
    	return 100 * ( max2 * 2 / (1 + c * Math.exp(-(input + max1 / 2))) - max2 );
    }
    
    public static double logisticSmall(double max1, double max2, double input) {
    	double c = 1.0 / Math.exp(-max1 / 2);
    	return 1 * ( max2 * 2 / (1 + c * Math.exp(-(input + max1 / 2))) - max2 );
    }
}

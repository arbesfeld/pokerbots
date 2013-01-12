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

}

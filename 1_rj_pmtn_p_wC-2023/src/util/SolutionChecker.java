package util;

import java.util.Arrays;

import data.Instance;

import data.Solution;

public class SolutionChecker {

	/**
	 * Returns true iff the solution is valid for given instance.
	 * @param solution
	 * @param instance
	 * @return true iff the solution is valid for given instance
	 */
	public static boolean isValid(Solution solution, Instance instance) {
		
		int j;
		
		if (solution==null || instance==null || solution.completion==null 
				|| solution.completion.length!=instance.n)
			return false;

        // cost verification
        int c = 0;
        for (j = 0; j < instance.n; j++) {
            c += instance.getCost( j, solution.completion[j]);
        }
        if (c != solution.cost) {
            return false;        
        }
        
        Integer[] cOrder = new Integer[instance.n];
        for (j=0;j<instance.n;j++)
        	cOrder[j] = j;
        CritComparator cComparator = new CritComparator( solution.completion );
        
        Arrays.sort(cOrder, cComparator);

        int sum = 0;
        int minRj = Integer.MAX_VALUE;
        for (Integer i : cOrder) {
         
            minRj = Math.min(minRj, instance.r[i]);
            sum += instance.p;
            if (minRj + sum > solution.completion[i]) {
                return false;
            }
        }

        return true;
		
	}
	
	
}

/**
 * InstanceGenerator.java
 */
package util;

import java.util.Random;

import data.ExtendedInstance;

/**
 * @author D. Rivreau
 *
 * 31 mars 08
 */
public class ExtendedInstanceGenerator {

    private static Random alea = new Random();
    
    public static ExtendedInstance createInstance(int n, int pmax, int wmax, boolean deadlines, boolean randomMode) {
    	
    	int p = alea.nextInt(pmax-1)+2;
    	
    	int[] r = new int[n];
    	
    	
    	
    	for (int i=1;i<n;i++) {
    		r[i] = alea.nextInt(p-1)+r[i-1]+1;
    		
    	}
    	int T = r[n-1]+p*n;
    	
    	int[][] w = new int[n][T+1];
    	
    	// Instance wC
    	if (!randomMode) {
    		for (int i=0;i<n;i++) {
    			int w_i = alea.nextInt(wmax)+1;
    			int d_i = 0;
    			if (deadlines)
    				d_i = alea.nextInt(T);
    			for (int t=0;t<=T;t++) {
    				if (t-p>=r[i])
    					//w[i][t] = Math.max(t - d_i, 0) * w_i;
    					w[i][t] = (t - d_i) * w_i;
    				else
    					w[i][t] = Integer.MAX_VALUE;
    			}
    		}   		
    	}
    	// Instance coûts aléatoires
    	else {
    		for (int i=0;i<n;i++) for (int t=0;t<=T;t++) { 
    			if (t-p>=r[i])
    				w[i][t] = alea.nextInt(wmax)+1;
    			else
    				w[i][t] = Integer.MAX_VALUE;
    			
    		}
    	}
    	
    	return new ExtendedInstance(n, p, r, w, T);
    	
    }
	
	
}

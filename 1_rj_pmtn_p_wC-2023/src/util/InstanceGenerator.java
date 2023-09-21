/**
 * InstanceGenerator.java
 */
package util;

import java.util.Random;

import data.Instance;

/**
 * @author D. Rivreau
 *
 * 31 mars 08
 */
public class InstanceGenerator {

    private static Random alea = new Random();
    
    public static Instance createInstance(int n, int pmax, int wmax) {
    	
    	int p = alea.nextInt(pmax-1)+2;
    	
    	int[] r = new int[n];
    	int[] w = new int[n];
    	
    	w[0] = alea.nextInt(wmax)+1;
    	for (int i=1;i<n;i++) {
    		r[i] = alea.nextInt(p-1)+r[i-1]+1;
    		w[i] = alea.nextInt(wmax)+1;
    	}
    	
    	return new Instance(n, p, r, w);
    	
    }
	
	
}

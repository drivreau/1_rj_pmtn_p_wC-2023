/**
 * Solution.java
 */
package data;

import java.util.Arrays;


/**
 * @author D. Rivreau
 *
 * 31 mars 08
 */
public class Solution {

    public int[] completion;
    public int cost;
 
    public Solution(int[] completion, int cost) {
    	this.completion = completion;
    	this.cost = cost;
    }
    
    public Solution(int n) {
        initialize(n);
    }

    public void initialize(int n) {
        if (completion == null || completion.length != n) {
            completion = new int[n];
        }
        cost = 0;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "C = " + Arrays.toString(completion) + "\ncost = " + cost;
    }
}

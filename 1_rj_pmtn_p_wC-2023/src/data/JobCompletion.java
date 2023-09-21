/**
 * JobCompletion.java
 */
package data;

/**
 * @author D. Rivreau
 *
 * 31 mars 08
 */
public class JobCompletion {
	
	public int job;

	public int completion;

	/**
	 * @param job
	 * @param completion
	 */
	public JobCompletion(int job, int completion) {
		super();
		this.job = job;
		this.completion = completion;
	}

    @Override
    public String toString() {
        return "[job="+job+", comp="+completion+"]";
    }
        
        
}

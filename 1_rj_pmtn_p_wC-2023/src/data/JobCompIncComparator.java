/**
 * JobCompDecComparator.java
 */
package data;

import java.util.Comparator;

/**
 * @author D. Rivreau
 *
 * 31 mars 08
 */
public class JobCompIncComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        JobCompletion jc1 = (JobCompletion) o1;
        JobCompletion jc2 = (JobCompletion) o2;
        if (jc1.completion != jc2.completion) {
            return jc1.completion - jc2.completion;
        } else {
            return jc1.job - jc2.job;
        }
    }

}

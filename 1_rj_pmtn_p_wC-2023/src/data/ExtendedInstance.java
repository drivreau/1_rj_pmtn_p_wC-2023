/**
 * Instance.java
 */
package data;

import java.util.Arrays;
import java.util.List;

/**
 * @author D. Rivreau
 *
 *         31 mars 08
 */
public class ExtendedInstance implements Cloneable {

	public int n;
	public int p;
	public int[] r;
	public int[] d;
	public int[][] w;
	public int T;

	public ExtendedInstance(int n) {
		initialize(n);
	}

	public ExtendedInstance(int n, int p, int[] r, int[][] w, int T) {
		this.n = n;
		this.p = p;
		this.r = r;
		this.w = w;
		d = new int[n];
		this.T = T;
	}

	public ExtendedInstance(int n, int p, int[] r, int[][] w, int d[], int T) {
		this.n = n;
		this.p = p;
		this.r = r;
		this.w = w;
		this.d = d;
		this.T = T;
	}
	
	public ExtendedInstance(int n, int p, int[] r, int[] wRef) {
		this.n = n;
		this.p = p;
		this.r = r;
		w = new int[n][];
		T = r[n-1]+n*p;
		for (int i=0;i<n;i++) {
			w[i] = new int[T+1];
			for (int t = 0; t <= T; t++)
				if (t < r[i]+p)
					w[i][t] = Integer.MAX_VALUE;
				else
					w[i][t] = wRef[i]*t;
		}
	}

	public void initialize(int n) {
		if (this.n != n) {
			this.n = n;
			r = new int[n];
			w = new int[n][];
			d = new int[n];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("n = " + n + "\nT = " + T + "\np = " + p + "\nr = " + Arrays.toString(r) + "\nw = [");
		for (int i=0;i<n;i++)
			buf.append(Arrays.toString(w[i])+"," );
		buf.append( "]\nd = "+ Arrays.toString(d) );
		return buf.toString();
	}

	@Override
	public ExtendedInstance clone() throws CloneNotSupportedException {
		ExtendedInstance toReturn = (ExtendedInstance) super.clone();
		toReturn.r = Arrays.copyOf(r, n);
		toReturn.w = Arrays.copyOf(w, n);
		toReturn.d = Arrays.copyOf(d, n);
		return toReturn;
	}

	public int getCost(int j, int completion) {
		return w[j][completion]; //Math.max(completion - d[j], 0) * w[j];
	}

	
}

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
public class Instance implements Cloneable {

	public int n;
	public int p;
	public int[] r;
	public int[] d;
	public int[] w;

	public Instance(int n) {
		initialize(n);
	}

	public Instance(int n, int p, int[] r, int[] w) {
		this.n = n;
		this.p = p;
		this.r = r;
		this.w = w;
		d = new int[n];
	}

	public Instance(int n, int p, int[] r, int[] w, int d[]) {
		this.n = n;
		this.p = p;
		this.r = r;
		this.w = w;
		this.d = d;
	}

	public void initialize(int n) {
		if (this.n != n) {
			this.n = n;
			r = new int[n];
			w = new int[n];
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
		return "n = " + n + "\np = " + p + "\nr = " + Arrays.toString(r) + "\nw = " + Arrays.toString(w) + "\nd = "
				+ Arrays.toString(d);
	}

	@Override
	public Instance clone() throws CloneNotSupportedException {
		Instance toReturn = (Instance) super.clone();
		toReturn.r = Arrays.copyOf(r, n);
		toReturn.w = Arrays.copyOf(w, n);
		toReturn.d = Arrays.copyOf(d, n);
		return toReturn;
	}

	public int getCost(int j, int completion) {
		return Math.max(completion - d[j], 0) * w[j];
	}

	public void setW(int i, int wi) {
		w[i] = wi;
	}

	public int delta(int i, int j) {
		int delta = 0;
		for (int k = i + 1; k <= j; k++)
			delta += Math.max(r[i] + p - r[k], 0) * w[k];
		delta -= p * (j - i) * w[i];
		return delta;
	}

	public int delta(int i, int j, List<Integer> l) {
		int delta = 0;
		int cpte = 0;
		for (int k = i + 1; k <= j; k++) if (l.contains(k)) {
			delta += Math.max(r[i] + p - r[k], 0) * w[k];
			cpte++;
		}
		delta -= p * cpte * w[i];
		return delta;
	}
	
	public int sigDelta(int i, int j) {
		int delta = delta(i, j);
		if (delta > 0)
			return 1;
		else if (delta < 0)
			return -1;
		else
			return 0;
	}

}

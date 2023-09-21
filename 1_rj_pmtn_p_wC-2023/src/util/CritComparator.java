package util;

import java.util.Comparator;

public class CritComparator implements Comparator<Integer> {

	/** Crit�re entier pour le tri. */
	private int[] w;

	/** Cr�e un comparator avec le critere w. */
	public CritComparator(int[] w) {
		this.w = w;
	}

	/** Fonction de comparaison. */
	@Override
	public int compare(Integer i, Integer j) {
		return w[i] - w[j];
	}
}

package simplex;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

import Jama.Matrix;

public class BasisExplore {

	static BufferedWriter f;
	
	static double[][] A = {
			{1,1,0,0,0,0,0,0,1,0,0,0,0,0},
			{0,0,1,1,1,0,0,0,0,1,0,0,0,0},
			{0,0,0,0,0,1,1,1,0,0,1,0,0,0},
			{1,0,1,0,0,1,0,0,0,0,0,1,0,0},
			{0,0,1,1,0,1,1,0,0,0,0,0,1,0},
			{1,1,1,1,1,1,1,1,0,0,0,0,0,1}
	};
	
	static double[] b = {1, 1, 1, 1, 1, 2};

	static Matrix B;

	static Matrix subMatrix(double[][] M, LinkedList<Integer> basis) {
		Matrix res = new Matrix(M.length,basis.size());
		int k = 0;
		for (int col : basis) {
			for (int row=0; row < M.length; row++) {
				res.set(row, k, M[row][col]);
			}
			k++;
		}
		return res;
	}
	
	static int nb = 0;
	
	static void afficher( int x, int a, int b, LinkedList<Integer> l ) throws IOException {
		if (x==0) {
			Matrix m = subMatrix(A,l);
			++nb;
			if (Math.abs(m.det())>1.01) {
				System.out.println(nb+ " ; "+l.toString()+" ; "+m.det() );
				m.print(NumberFormat.getInstance(Locale.ENGLISH),4);
				Matrix inv = m.inverse();
				inv.print(NumberFormat.getInstance(Locale.ENGLISH),4);
				Matrix sol = inv.times(B);
				sol.print(NumberFormat.getInstance(Locale.ENGLISH),4);
				f.write(nb + " ; "+l.toString()+" ; "+m.det());
				f.newLine();
			}
		}
		else {
			l.addLast(a);
			afficher( x-1, a+1, b, l );
			l.removeLast();
			if (b-a+1>x)
				afficher( x, a+1, b, l);
		}
	}
	
	public static void main(String[] args) throws IOException {
		
			B = new Matrix( b, b.length );
			
			B.print(NumberFormat.getInstance(Locale.ENGLISH),4);
		
			f = new BufferedWriter( new FileWriter( "res.csv") );
	
			afficher( 6, 0, 13, new LinkedList<Integer>() );
		
			f.close();

	}

}

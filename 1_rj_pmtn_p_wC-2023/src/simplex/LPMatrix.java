package simplex;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.TreeMap;

import Jama.Matrix;
import data.Instance;
import data.JobCompIncComparator;
import data.JobCompletion;
import data.Solution;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class LPMatrix {

	private static NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
	private Matrix A;
	private Matrix b;
	private String[] colNames;
	private int[] rows;
	private int[] cols;
	private int n;
	private int p;
	private int[] coltime;
	private int[] coljob;
	private TreeSet<JobCompletion> unionP;
	private TreeSet<Integer> unionC;
	private long CPTE;
	private TreeMap<Integer,Long> stat;

	public LPMatrix(Instance data) {
		buildModel(data);
	}

	public LPMatrix(double[][] A, double[] b, String[] colNames, int n, int p) {
		init(A, b, colNames, n, p);
	}

	private void init(double[][] A, double[] b, String[] colNames, int n, int p) {
		this.colNames = colNames.clone();
		this.A = new Matrix(A);
		this.b = new Matrix(b, b.length);
		rows = new int[b.length];
		for (int r = 0; r < b.length; r++)
			rows[r] = r;
		cols = new int[b.length];
		this.n = n;
		this.p = p;
		CPTE = 0;
		stat = new TreeMap<Integer,Long>();
	}

	private void findFractional(int x, int a, int b, LinkedList<Integer> l) {
		if (x == 0) {
			for (int c = 0; c < l.size(); c++)
				cols[c] = l.get(c);
			Matrix m = A.getMatrix(rows, cols);
			double det =m.det();
			updateStats(det);
			if (CPTE%1000000==0)
				m.print(format,4);
			if (Math.abs(det) > 1.01) {
				System.out.print(l.toString() + " ; " + det);
				//m.print(format, 4);
				Matrix inv = m.inverse();
				//inv.print(format, 4);
				Matrix sol = inv.times(this.b);
				//sol.print(format, 4);
				Matrix BA = inv.times(A);
				//printLPModel(BA, l);
				
				AuxLP aux  = new AuxLP(BA, l);
				aux.setMuteMode(true);
				double[] res =aux.getSolution();
				System.out.println( " ; res = "+Arrays.toString(res) );
				if (res!=null) {
					m.print(format, 4);
					inv.print(format, 4);
					sol.print(format, 4);
					printLPModel(BA, l);
				
					System.out.println("*******************************");
					System.out.print("A = ");
					A.print( format, 4);
					
					System.out.print("BA = ");
					BA.print( format, 4);
					
					System.out.print("b = ");
					this.b.print( format, 4);
					
					System.out.println("colNames = "+Arrays.toString(colNames));
					System.out.println("coltime = "+Arrays.toString(coltime));
					System.out.println("coljob = "+Arrays.toString(coljob));
					
					System.exit(0);
				}
			}
		} else {
			l.addLast(a);
			
			for (int c = 0; c < l.size(); c++)
				cols[c] = l.get(c);
			Matrix m = A.getMatrix(rows, cols);
			/*
			if (l.size()>=5 && l.contains(0))
				System.out.println("cols = "+l+", rank = "+m.rank());
				*/
			if (m.rank()>=l.size())
				findFractional(x - 1, a + 1, b, l);
			
			l.removeLast();
			if (b - a + 1 > x)
				findFractional(x, a + 1, b, l);
		}
	}

	private void updateStats(double det) {
		CPTE++;
		
		int v = (int)Math.round(det);
		if (stat.containsKey(v)) {
			stat.put(v, stat.get(v)+1);
		}
		else stat.put(v,1l);
			
		
		if (CPTE % 1000000==0) {
			for (int a : stat.keySet())
				System.out.println( "nb det "+a+" = "+stat.get(a));			
		}
			
		
	}

	private void printLPModel(Matrix BA, LinkedList<Integer> l) {
		
		System.out.println("**********************");
		System.out.print("min: ");
		for (int c = 0; c < A.getColumnDimension() - A.getRowDimension(); c++)
			System.out.print(" + " + cost(colNames[c]));
		System.out.println(";");
		for (int c = 0; c < A.getColumnDimension() - A.getRowDimension(); c++)
			System.out.println(cost(colNames[c]) + " >= 1;");
		for (int c = 0; c < A.getColumnDimension(); c++)
			if (!l.contains(c)) {
				if (c < A.getColumnDimension() - A.getRowDimension())
					System.out.print(cost(colNames[c]));
				else
					System.out.print("    ");
				for (int r = 0; r < A.getRowDimension(); r++)
					if (l.get(r) < A.getColumnDimension() - A.getRowDimension()) if (Math.abs(BA.get(r, c))>0.01){
						System.out.print(coef(-BA.get(r, c)));
						System.out.print("*");
						System.out.print(cost(colNames[l.get(r)]));
					}
				System.out.println(" <= -0.1;");
			}
		printSumWiCiConstraints();
		System.out.println("**********************");
	}

	private void printSumWiCiConstraints() {
		
		for (int c=0;c<A.getColumnDimension()-A.getRowDimension();c++) {
			String s = colNames[c].replaceAll("x", " ").replaceAll("_", " ");
			Scanner sc = new Scanner(s);
			int i = sc.nextInt();
			int t = sc.nextInt();
			System.out.print(cost(colNames[c])+ " = " );
			System.out.println( (n*p-t) + " * w" + i + ";" );			
		}
	}

	private static String coef(double d) {
		if (Math.abs(d) < 0.01)
			return "";
		else if (d > 0)
			return " + " + d;
		else
			return " - " + (-d);
	}

	private static String cost(String s) {
		return s.replaceFirst("x", "c");
	}

	public void findFractional() {
		findFractional(A.getRowDimension(), 0, A.getColumnDimension() - 1, new LinkedList<Integer>());
	}

	public void print() {
		System.out.println(Arrays.toString(colNames));
		A.print(format, 4);
		b.print(format, 4);
	}

	

	private void buildCompletions(Instance instance) {
		
		unionP = new TreeSet<JobCompletion>(new JobCompIncComparator());
		unionC = new TreeSet<Integer>();
		int e_j;
		int T = instance.n * instance.p;

		for (int j = 0; j < instance.n; j++) {

			e_j = j + 1;
			while (e_j < instance.n && instance.r[e_j] < instance.r[j] + (e_j - j) * instance.p)
				e_j++;

			for (int k = 1; k <= e_j - j; k++) {
				unionC.add(instance.r[j] + k * instance.p);
			}
			for (Integer c : unionC) {
				if (c >= instance.r[j] + instance.p && c < T) {
					unionP.add(new JobCompletion(j, c));
				}
			}
		}

	}

	public void buildModel(Instance instance) {
		int j, col;
		int n = instance.n;
		int T = instance.n * instance.p;

		buildCompletions(instance);

		int Ncol = unionP.size();
		int Nrow = n * (n + 1) / 2;

		double A[][] = new double[Nrow][Ncol + Nrow];
		double b[] = new double[Nrow];
		colNames = new String[Ncol + Nrow];
		coltime = new int[Ncol + Nrow];
		coljob = new int[Ncol + Nrow];

		col = 0;
		for (JobCompletion jc : unionP) {
			col++;
			colNames[col - 1] = "x" + (jc.job + 1) + "_" + jc.completion;
			coltime[col - 1] = jc.completion;
			coljob[col - 1] = jc.job+1;
		}
		for (j = 0; j < instance.n; j++) {
			col++;
			colNames[col - 1] = "x" + (j + 1) + "_" + T;
			coltime[col - 1] = T;
			coljob[col - 1] = j + 1;
		}
		for (int a = 1; a < instance.n; a++)
			for (int i = 1; i <= instance.n - a; i++) {
				col++;
				colNames[col - 1] = "y" + a + "_" + i;
				coltime[col - 1] = 0;
				coljob[col - 1] = 0;
			}

		for (j = 0; j < instance.n; j++) {
			for (col = 0; col < Ncol + Nrow; col++) {
				if (coljob[col] == j + 1) {
					A[j][col] = 1.0;
				}
			}
			b[j] = 1;
		}

		int r = instance.n;

		for (int a = 1; a < instance.n; a++)
			for (int i = 1; i <= instance.n - a; i++) {
				for (col = 0; col < Ncol; col++) {
					if ((coljob[col] >= i) && (coltime[col] < instance.r[i - 1] + (a + 1) * instance.p)) {
						A[r][col] = 1.0;
					}
				}
				A[r][Ncol + r] = 1.0;
				b[r] = a;
				r++;
			}
		init(A, b, colNames, n, instance.p);
	}
	
	
	public class AuxLP {

		/**
		 * Cplex object for IP model.
		 */
		private IloCplex cplex;

		/**
		 * Variables x.
		 */
		private IloNumVar[] x;
		
		/**
		 * Variables w.
		 */
		private IloNumVar[] w;

		/**
		 * Turn off cplex messages if mute is true;
		 */
		private boolean muteMode;

		/**
		 * Time limit in seconds (if <= 0.0 no time limit is set)
		 */
		private double timeLimit;

		private Matrix BA;
		
		private LinkedList<Integer> basis;
		
		/**
		 * Default constructor.
		 */
		public AuxLP(Matrix BA, LinkedList<Integer> l) {
			super();
			this.BA = BA;
			this.basis = l;
		}

		
		/**
		 * Creates variables.
		 * 
		 * @param instance
		 *            TSP instance to solve
		 * @throws IloException
		 */
		private void createVariables() throws IloException {

			// création de variables 
			x = cplex.numVarArray(A.getColumnDimension(),0,Double.MAX_VALUE,colNames);
			String wNames[] = new String[n];
			for (int i=1;i<=n;i++)
				wNames[i-1] = "w"+i;
			w = cplex.numVarArray(n,0,Integer.MAX_VALUE,wNames);
			//w = cplex.intVarArray(n,0,2100000000,wNames);	// Tentative 19/12/19 
		}

		/**
		 * Creates constraints.
		 * 
		 * @param instance
		 *            TSP instance to solve
		 * @throws IloException
		 */
		private void createConstraints() throws IloException {

			// création d'un objet manipulant des expressions linéaires entières
			IloLinearNumExpr expr = cplex.linearNumExpr();

			int Ncol = unionP.size();
			int Nrow = n * (n + 1) / 2;

			for (int c = 0; c < A.getColumnDimension() - A.getRowDimension(); c++) {
				expr.clear();
				expr.addTerm(1, x[c]);
				cplex.addGe(expr, 0.1);
			}

			for (int c = 0; c < A.getColumnDimension(); c++)
				if (!basis.contains(c)) {
					expr.clear();
					if (c < A.getColumnDimension() - A.getRowDimension())
						expr.addTerm(1, x[c]);
					for (int r = 0; r < A.getRowDimension(); r++)
						if (basis.get(r) < A.getColumnDimension() - A.getRowDimension()) if (Math.abs(BA.get(r, c))>0.01){
							expr.addTerm(-BA.get(r, c),x[basis.get(r)]);
						}
					cplex.addLe(expr,-0.1);
					//cplex.addGe(expr,0); // DR 19/12/19
				}
		
	
			// contraintes sup
			/*
			for (int col = 0; col < Ncol + Nrow; col++) {
				if (coljob[col] > 0) {
			
					expr.clear();
					int i = coljob[col]-1;
					int t = coltime[col];
					expr.addTerm(x[col], 1);
					expr.addTerm( (t-n*p), w[i]);
					cplex.addEq(expr, 0);
				}
			}
			*/
			
			
		}

		/**
		 * Creates objective.
		 * 
		 * @param instance
		 *            TSP instance to solve
		 * @throws IloException
		 */
		private void createObjective() throws IloException {

			IloLinearNumExpr expr = cplex.linearNumExpr();

			for (int c = 0; c < A.getColumnDimension() - A.getRowDimension(); c++)
				expr.addTerm(x[c], 1);

						
			// on minimisise l'objectif
			cplex.addMinimize(expr);

		}

		/**
		 * Gets solution.
		 * 
		 * @throws IloException
		 */
		private double[] innerGetSolution() throws IloException {

			int Ncol = unionP.size();
			int Nrow = n * (n + 1) / 2;
			double[] resu = new double[Ncol + Nrow];

			for (int j = 0; j < n; j++) {
				for (int col = 0; col < Ncol + Nrow; col++) {
					try {
						if (coljob[col] == j + 1 && cplex.getValue(x[col])>0.01) {
						resu[col] = cplex.getValue(x[col]);
						}
					} catch (Exception e) {
						System.out.println( "Bizarre : col="+col+", coljob="+coljob[col]+", coltime="+coltime[col]);
					}
				}
			}

			// on génère la solution associée à la tournée calculée
			return resu;

		}

		/**
		 * Fixes if Cplex gives messages on console.
		 * 
		 * @param muteMode
		 *            the muteMode to set
		 */
		public void setMuteMode(boolean muteMode) {
			this.muteMode = muteMode;
		}

		/**
		 * Fixes maximum time for solving a problem. If timeLimit = 0 then there is
		 * no limit.
		 * 
		 * @param timeLimit
		 *            the timeLimit to set
		 */
		public void setTimeLimit(double timeLimit) {
			this.timeLimit = timeLimit;
		}

		
		public double[] getSolution() {

			double[] res = null;

			try {
				// on créé l'objet qui gère le modèle
				cplex = new IloCplex();

				// on configure l'affichage (muet ou pas)
				if (muteMode)
					cplex.setOut(null);

				// on fixe éventuellement un temps limite
				if (timeLimit > 0)
					cplex.setParam(IloCplex.DoubleParam.TiLim, timeLimit);

				// on créé les variables
				createVariables();

				// on créé les contraintes
				createConstraints();

				// on créé la fonction objectif
				createObjective();

				// on lance le solveur
				if (cplex.solve()) {
					// une solution est disponible : on la récupère
					res = innerGetSolution();
		
					if (cplex.getStatus() != IloCplex.Status.Optimal)
						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
								.warning(getClass().getSimpleName() + " -> solution non optimale sur modèle");	
				} else {
					/*
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning(
							getClass().getSimpleName() + " -> pas de solution trouvée sur modèle, " + cplex.getStatus());
					*/
				}

				//cplex.exportModel("last.lp");
				
				// nettoyage de fin
				cplex.end();

			} catch (IloException e) {
				e.printStackTrace();
			}

			return res;

		}

	}

	
	
	
	
	public static void main(String[] args) {
	
		/*
		double[][] A = { { 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0 }, { 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0 },
				{ 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1 } };

		double[] b = { 1, 1, 1, 1, 1, 2 };

		String[] colNames = { "x1_3", "x1_6", "x2_4", "x2_6", "x2_7", "x3_5", "x3_6", "x3_7", "x1_9", "x2_9", "x3_9",
				"y1_1", "y1_2", "y2_1" };

		LPMatrix lp = new LPMatrix(A, b, colNames, 3, 3);
		*/
		
		
		int[] r = {0,1,2,3};
		int[] w = {1,2,3,4};
		int n = r.length;
		int p = 4;
		
		/*
		int[] r = {0,1,2,3,4};
		int[] w = {1,2,3,4,5};
		int n = r.length;
		int p = 5;
		*/
		
		/*
		
		int[] r = {0,1,2};
		int[] w = {1,2,3};
		int n = r.length;
		int p = 3;
		*/
		Instance instance = new Instance(n, p, r, w);
		LPMatrix lp = new LPMatrix(instance);
	
		lp.print();
		
		lp.findFractional();

	}
}

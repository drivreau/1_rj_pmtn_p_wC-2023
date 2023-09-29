package simplex;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.TreeMap;

import Jama.Matrix;
import data.ExtendedInstance;
import data.JobCompIncComparator;
import data.JobCompletion;
import data.Solution;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import util.ExtendedInstanceGenerator;

class ExtendedExplore {

	private static Random alea = new Random();

	/**
	 * Cplex object for IP model.
	 */
	private IloCplex cplex;

	/**
	 * Variables x. 1;::
	 */
	private IloNumVar[][] x;

	private ExtendedInstance data;

	public ExtendedExplore(ExtendedInstance data) {
		this.data = data;
	}

	/**
	 * Creates variables.
	 * 
	 * @param instance TSP instance to solve
	 * @throws IloException
	 */
	private void createVariables(boolean binary) throws IloException {

		// cr�ation de variables
		x = new IloNumVar[data.n][];
		for (int i = 0; i < data.n; i++) {
			String names[] = new String[data.T + 1];
			for (int t = 0; t <= data.T; t++)
				names[t] = "x" + i + "_" + t;
			if (binary)
				x[i] = cplex.boolVarArray(data.T + 1, names);
			else
				x[i] = cplex.numVarArray(data.T + 1, 0, 1, names);
		}

	}

	/**
	 * Creates constraints.
	 * 
	 * @param instance TSP instance to solve
	 * @throws IloException
	 */
	private void createConstraints1() throws IloException {
		// cr�ation d'un objet manipulant des expressions lin�aires enti�res
		IloLinearNumExpr expr = cplex.linearNumExpr();
		for (int j = 0; j < data.n; j++) {
			expr.clear();
			for (int t = 0; t <= data.T; t++)
				if (data.w[j][t] < Integer.MAX_VALUE)
					expr.addTerm(1, x[j][t]);
			cplex.addEq(expr, 1);
		}
	}

	/**
	 * Creates constraints.
	 * 
	 * @param instance TSP instance to solve
	 * @throws IloException
	 */
	private void createConstraints2(boolean randomMode) throws IloException {
		// cr�ation d'un objet manipulant des expressions lin�aires enti�res
		IloLinearNumExpr expr = cplex.linearNumExpr();

		for (int a = 1; a < data.n; a++)
			for (int i = 0; i <= data.n - a; i++) {
				expr.clear();
				for (int j = i; j < data.n; j++)
					for (int t = 0; t < data.r[i] + (a + 1) * data.p; t++)
						if (data.w[j][t] < Integer.MAX_VALUE)
							expr.addTerm(1, x[j][t]);
				if (randomMode)
					cplex.addLe(expr, alea.nextInt(data.n));
				else
					cplex.addLe(expr, a);
			}
	}

	/**
	 * Creates objective.
	 * 
	 * @param instance TSP instance to solve
	 * @throws IloException
	 */
	private void createObjective() throws IloException {

		IloLinearNumExpr expr = cplex.linearNumExpr();

		for (int j = 0; j < data.n; j++)
			for (int t = 0; t <= data.T; t++)
				if (data.w[j][t] < Integer.MAX_VALUE)
					expr.addTerm(data.w[j][t], x[j][t]);

		// on minimisise l'objectif
		cplex.addMinimize(expr);

	}

	public double getSolution(boolean binaryMode, boolean secondHandRandom) {

		double res = -1;

		try {
			// on cr�� l'objet qui g�re le mod�le
			cplex = new IloCplex();

			// on cr�� les variables
			createVariables(binaryMode);

			// on cr�� les contraintes
			createConstraints1();

			createConstraints2(secondHandRandom);

			// on cr�� la fonction objectif
			createObjective();

			// on lance le solveur
			if (cplex.solve()) {

				res = cplex.getObjValue();

				if (cplex.getStatus() != IloCplex.Status.Optimal)
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
							.warning(getClass().getSimpleName() + " -> solution non optimale sur mod�le");
			} else {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning(
						getClass().getSimpleName() + " -> pas de solution trouv�e sur mod�le, " + cplex.getStatus());
			}

			cplex.exportModel("last.lp");

			// nettoyage de fin
			cplex.end();

		} catch (IloException e) {
			e.printStackTrace();
		}

		return res;

	}

	public static void main(String[] args) {

		/*
		 * double[][] A = { { 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1,
		 * 1, 0, 0, 0, 0, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0 },
		 * { 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 1, 1, 0, 1, 1, 0, 0, 0,
		 * 0, 0, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1 } };
		 * 
		 * double[] b = { 1, 1, 1, 1, 1, 2 };
		 * 
		 * String[] colNames = { "x1_3", "x1_6", "x2_4", "x2_6", "x2_7", "x3_5", "x3_6",
		 * "x3_7", "x1_9", "x2_9", "x3_9", "y1_1", "y1_2", "y2_1" };
		 * 
		 * LPMatrix lp = new LPMatrix(A, b, colNames, 3, 3);
		 */

		int[] r = { 0, 1, 2, 3 };
		int[] w = { 1, 2, 3, 4 };
		int n = r.length;
		int p = 4;
		
		
		

		/*
		 * int[] r = {0,1,2,3,4}; int[] w = {1,2,3,4,5}; int n = r.length; int p = 5;
		 */

		/*
		 * 
		 * int[] r = {0,1,2}; int[] w = {1,2,3}; int n = r.length; int p = 3;
		 */
		
		
		//ExtendedInstance instance = new ExtendedInstance(n, p, r, w);
		n = 10;
		for (int k=0;k<1000000;k++) {
		
			ExtendedInstance instance = ExtendedInstanceGenerator.createInstance(n, p, 100, true, false);
		
			ExtendedExplore lp = new ExtendedExplore(instance);
		
			double res1 = lp.getSolution(false,true);
			long res2 = Math.round(res1);
		
			System.out.println( "k = "+k+", Valeur1 = "+res1+", Valeur2 = "+res2);
			
			if (Math.abs(res2-res1)>0.05) {
				System.out.println( "Found !" );
				System.out.println( instance.toString() );
				break;
			}
		
		
		}
		 
		 
		
		
		
		
		/*
		 * Instance instance = new Instance(n, p, r, w); ExtendedExplore lp = new
		 * ExtendedExplore(instance);
		 * 
		 * lp.print();
		 * 
		 * lp.findFractional();
		 */
	}
}

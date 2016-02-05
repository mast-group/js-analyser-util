/* ===========================================================
 * EM : a clustering program for the Java(TM) platform
 * ===========================================================
 * This is a Java code that borrows Michael Chen's idea (sth4nth@gmail.com).
 * http://www.mathworks.com/matlabcentral/
 * fileexchange/26184-em-algorithm-for-gaussian-mixture-model
 * 
 * This is a Java program trying to obtain the maximum likelihood estimation 
 * of Gaussian mixture model by expectation maximization (EM) algorithm.

 * It works on data set of arbitrary dimensions. 
 * Several techniques are applied to avoid the float number underflow problems 
 * that often occurs on computing probability of high dimensional data.
 * -----------------
 * EM.java
 * -----------------
 *
 * Original Author:  Sun Bo (National University of Singapore)
 * Contact: sunbocsg@gmail.com
 * Date: July 19,2012
 * 
 * Core Reference: http://www-personal.umich.edu/~gyemin/pubs/tcem_tr.pdf 
 * The first two pages should be enough.
 * 
 * I hope that it will be useful, but it is WITHOUT ANY WARRANTY.
 * 
 * How to use it?
 * 
 * 1. instantiate an EM object.
 * 2. set your tolerance level,default is 1e-10
 * 3. construct your data matrix, see bulidCluster method 
 * for more details
 * 4. bulid clusters
 * 5. if converged, call getLabel to get the final assignment
 */
package em;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import JSci.maths.matrices.AbstractDoubleMatrix;
import JSci.maths.matrices.DoubleDiagonalMatrix;
import JSci.maths.matrices.DoubleSquareMatrix;
import JSci.maths.vectors.DoubleVector;

public class EM {
   /* public static double tol = 1e-10; // tolerance level
    public static final int maxiter = 500; // maximum iteration
    private static DoubleDiagonalMatrix stable; // for computational stability
    private boolean converged;
    private double llh, previousllh;
    private int count, dimension, numOfVector, numOfCluster;
    // This array stores the label of assignment for each data, if converged
    private int[] finalAssignment;

    // this has several names: weight or mixtur portion
    private double[] weight;

    // membership probability
    private double[][] memberProb;

    private MyAbstractDoubleVector[] data; // input data
    private MyAbstractDoubleVector[] mu; // mean or center
    private AbstractDoubleMatrix[] sigma;// covariance matrices

    *//*
     * constructor
     *//*
    public EM() {
        converged = false;
        count = 1;
    }

    *//*
     * random initialization, assign k distinct data point to be centers please
     * see RandomSample class for details
     *//*
    private void initialization(MyAbstractDoubleVector[] data, int init) {
        int k = init, n = numOfVector;
        mu = new MyAbstractDoubleVector[k];
        // idx is an array storing distinct k random values ranging from 1 to n
        int[] idx = RandomSample.randomsample(n, k);
        for (int i = 0; i < k; i++) {
            mu[i] = data[idx[i]];
        }
		*//*
		 * The following part determines the initial assignment. During random
		 * initialization, the covariance matrix sigma is assumed to be identity
		 * matrix, so for each data x, the nomralization constant is identical,
		 * in order to determine the initial assigment, it suffices to take the
		 * minimum of Mahalanobis distance, m distance = (x - mu)'*sigma*(x -
		 * mu) = (x'-mu')(x-mu) = x'x - mu'x-x'mu + mu'mu = mu'mu - 2* mu'x +
		 * x'x since x'x is same for all cluster means, we only need to take the
		 * maximum of(mu'x-1/2*mu'mu), this is fundamental to understand the
		 * code
		 *//*
        double[][] temp = new double[n][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                temp[i][j] = mu[j].scalarProduct(data[i]) - 0.5
                        * mu[j].scalarProduct(mu[j]);
            }
        }
        int[] label = new int[n];
        for (int i = 0; i < n; i++) {
            label[i] = max(temp[i]);
        }
		*//*
		 * End of initial assignment
		 *//*

		*//*
		 * check if there is empty cluster, if so, redo the initialization
		 *//*
        int uniqueelement = unique(label);
        while (k != uniqueelement) {
            idx = RandomSample.randomsample(n, k);
            for (int i = 0; i < k; i++) {
                mu[i] = data[idx[i]];
            }
            temp = new double[n][k];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < k; j++) {
                    temp[i][j] = mu[j].scalarProduct(data[i]) - 0.5
                            * mu[j].scalarProduct(mu[j]);
                }
            }
            label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = max(temp[i]);
            }
            uniqueelement = unique(label);
        }
		*//*
		 * initialze the membership probability
		 *//*
        memberProb = new double[n][k];
        for (int i = 0; i < n; i++) {
            memberProb[i][label[i]] = 1;
        }
    }

    *//*
     * return the index of the maximum values in an array
     *//*
    private static int max(double[] array) {
        int idx = 0;
        double max = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                idx = i;
            }
        }
        return idx;
    }

    *//*
     * return the number of unique elements in an array. For example, if arr =
     * {1,2,3,4}, unique(arr) = 4; if arr = {1,1,2}, unique(arr) =2
     *//*
    private static int unique(int[] arr) {
        int ans = 0;
        Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (ht.get(arr[i]) == null) {
                ht.put(arr[i], 1);
            } else {

            }
        }
        ans = ht.size();
        return ans;
    }

    *//*
     * print a two-d array
     *//*
    public static void printArray(double[][] memberProb) {
        for (int i = 0; i < memberProb.length; i++) {
            for (int j = 0; j < memberProb[i].length; j++) {
                System.out.print(memberProb[i][j] + " ");
            }
            System.out.println("");
        }
    }

    private void maximization(double[][] memberProb, int n, int k, int d) {
        double[] temp = new double[k];
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < n; j++) {
                temp[i] += memberProb[j][i];
            }
        }
        // update weight or mixture portion
        for (int i = 0; i < k; i++) {
            weight[i] = temp[i] * 1.0 / n;
        }
        // update mean or center
        for (int h = 0; h < k; h++) {
            mu[h] = new DoubleVector(d);
            for (int i = 0; i < n; i++) {
                mu[h] = mu[h].plus(data[i].scale(memberProb[i][h]));
            }
            mu[h] = mu[h].scale(1.0 / temp[h]);
        }
        // update sigma
        sigma = new DoubleSquareMatrix[k];
        for (int i = 0; i < k; i++) {
            sigma[i] = new DoubleSquareMatrix(d);
        }
        for (int h = 0; h < k; h++) {
            for (int i = 0; i < n; i++) {
                MyAbstractDoubleVector tempVector = (data[i].minus(mu[h]))
                        .scale(Math.sqrt(memberProb[i][h]));
                sigma[h] = (DoubleSquareMatrix) sigma[h].add((tempVector
                        .multiply()));
            }
            sigma[h] = sigma[h].scalarDivide(temp[h]);

            sigma[h] = sigma[h].add(stable); // for numerical stability
            // System.out.print(sigma[h]);
        }

    }

    // expectation function, return the average loglikelihood of estimation
    private double expectation(int n, int k, int d) {
        double llh = 0;
        double[][] temp = new double[n][k];
        for (int h = 0; h < k; h++) {
			*//*
			 * we will use CholeskyDecomposation since sigma is positive
			 * definite. Use logarithm and exponential to compute membership
			 * probability
			 *//*
            AbstractDoubleMatrix U = ((DoubleSquareMatrix) sigma[h])
                    .choleskyDecompose()[0];
            AbstractDoubleMatrix inverse = ((DoubleSquareMatrix) U).inverse();
            double t = 0;
            for (int i = 0; i < d; i++) {
                t += Math.log(U.getElement(i, i));
            }
            double c = d * Math.log(2 * Math.PI) + 2 * t;
            for (int i = 0; i < n; i++) {
                MyAbstractDoubleVector diff = data[i].minus(mu[h]);
                AbstractDoubleMatrix Q = (AbstractDoubleMatrix) inverse
                        .multiply(diff.toMatrix().transpose());
                double q = Q.scalarProduct(Q); // Maha distance
                temp[i][h] = -0.5 * (c + q);
            }
        }

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                temp[j][i] += Math.log(weight[i]);
            }
        }
        // obtain the sum along rows
        double[] T = logsumexp(temp);

        for (int i = 0; i < T.length; i++) {
            llh += T[i];
        }
        llh = llh * 1.0 / n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                temp[i][j] = temp[i][j] - T[i];
                temp[i][j] = Math.exp(temp[i][j]);
            }
        }
        // update membership probability
        memberProb = temp;
        return llh;
    }

    // Compute log sum while avoiding numerical underflow.
    private double[] logsumexp(double[][] arr) {
        double[] ans = new double[arr.length];
        double[] y = new double[arr.length];
        double[][] temp = new double[arr.length][arr[0].length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = Arrays.copyOf(arr[i], arr[i].length);
        }
		*//*
		 * subtract the maximum of each row, at the end add it back
		 *//*
        for (int i = 0; i < temp.length; i++) {
            y[i] = max(temp[i]);
            for (int j = 0; j < temp[i].length; j++) {
                temp[i][j] -= y[i];
            }
        }
        for (int i = 0; i < temp.length; i++) {
            double sum = 0;
            for (int j = 0; j < temp[i].length; j++) {
                sum += Math.exp(temp[i][j]);
            }
            ans[i] = y[i] + Math.log(sum);
        }
        Vector<Integer> idx = new Vector<Integer>();
        for (int i = 0; i < y.length; i++) {
            if (Double.isInfinite(y[i]) || Double.isNaN(y[i])) {
                idx.add(i);
            }
        }
        if (idx.size() != 0) {
            for (int i = 0; i < idx.size(); i++) {
                ans[idx.get(i)] = y[idx.get(i)];
            }
        }
        return ans;
    }

    *//*
     * data matrix is an n x d matrix, where d is the dimension of each vector,
     * n is the number of vectors, k : number of clusters , for example,
     * {{1,2},{ 3,4}, {5,6}} represents three two-dimensional points
     *//*
    public void bulidCluster(double[][] array, int k) {
        numOfVector = array.length;
        dimension = array[0].length;
		*//*
		 * for numerical stability
		 *//*
        double[] arr = new double[dimension];
        for (int i = 0; i < dimension; i++)
            arr[i] = 1e-6;
        stable = new DoubleDiagonalMatrix(arr);
		*//**//*
        numOfCluster = k;
        data = new MyAbstractDoubleVector[numOfVector];
        weight = new double[k];
        *//**********************************************************//*
        System.out.println("EM for Gaussian mixture: running ... ");

        for (int i = 0; i < array.length; i++) {
            double[] temp = new double[dimension];
            temp = Arrays.copyOf(array[i], array[i].length);
            boolean isSparse = false;
            int zerocount = 0;
            for (int h = 0; h < temp.length; h++) {
                if (temp[h] == 0) {
                    zerocount++;
                }
            }
            isSparse = zerocount > 0.8 * temp.length;
            if (!isSparse)
                data[i] = new DoubleVector(temp);
            else
                data[i] = new DoubleSparseVector(temp);
        }
        initialization(data, numOfCluster);

        previousllh = Double.NEGATIVE_INFINITY;
        while (!converged && count < EM.maxiter) {
            count++;
            maximization(memberProb, numOfVector, k, dimension);
            llh = expectation(numOfVector, k, dimension);
            // relative
            converged = llh - previousllh < tol * Math.abs(llh);
            previousllh = llh;
        }

        if (converged) {
            finalAssignment = new int[numOfVector];
            for (int i = 0; i < numOfVector; i++) {
                finalAssignment[i] = max(memberProb[i]);
            }
        } else {
            System.out.println("NOT converged is 500 steps");
        }
    }

    // return the membership probability of each data point
    public double[][] getMembershipProbability() {
        return memberProb;
    }

    // return the mixture portion
    public double[] getWeight() {
        return weight;
    }

    *//*
     * NULL means not converged
     *//*
    public int[] getLabel() {
        return finalAssignment;
    }

    // return the convergence status
    public boolean isConverged() {
        return converged;
    }

    public void setRelativeTolenranceLevel(double eps) {
        tol = eps;
    }*/
}
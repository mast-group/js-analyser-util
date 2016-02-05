package em;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

/* This is borrowed from MatLab implementation.
 * 
 * RANDSAMPLE Random sampling, without replacement
 Y = RANDSAMPLE(N,K) returns K values sampled at random, without
 replacement, from the integers 1:N.
 * */

public class RandomSample {
    public static int[] randomsample(int n, int k) {
        int[] ans = new int[k];
		/*
		 * If the sample is a sizeable fraction of the population, just
		 * randomize the whole population (which involves a full sort of n
		 * random values), and take the first k.
		 */
        if (4 * k > n) {
            int[] temp = randperm(n);
            ans = Arrays.copyOf(temp, k);
        }
		/*
		 * If the sample is a small fraction of the population, a full sort is
		 * wasteful. Repeatedly sample with replacement until there are k unique
		 * values.
		 */
        else {
            int[] x = new int[n]; // flags
            int sumx = 0;
            while (sumx < k) {
                // sample w/replacement
                int t = k - sumx;
                Random r = new Random();
                for (int i = 0; i < t; i++) {
                    x[(int) Math.floor(n * r.nextDouble())] = 1;
                }

                // count how many unique elements so far
                for (int aX : x) {
                    sumx += aX;
                }
            }
            int[] temp = new int[k];
            Vector<Integer> temp1 = new Vector<Integer>();
            for (int i = 0; i < n && temp1.size() < k; i++) {
                if (x[i] > 0)
                    temp1.add(i);
            }
            for (int i = 0; i < temp1.size(); i++)
                temp[i] = temp1.get(i);
            int[] idx = randperm(k);
            for (int i = 0; i < idx.length; i++)
                ans[i] = temp[idx[i]];
        }
        return ans;
    }

    /*
     * randperm Random permutation.
     *
     * p = randperm(n) returns a random permutation of 1:n.
     */
    private static int[] randperm(int n) {
        int[] p = new int[n];
		/*
		 * use Vector to handle duplicate keys
		 */
        TreeMap<Double, Vector<Integer>> tree = new TreeMap<Double, Vector<Integer>>();
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            double key = r.nextDouble();
            Vector<Integer> vector = new Vector<Integer>();
            if (tree.get(key) == null) {
                vector.add(i);
                tree.put(key, vector);
            } else {
                Vector<Integer> temp = tree.get(key);
                temp.add(i);
                tree.put(key, vector);
            }
        }
        Vector<Integer> ans = new Vector<Integer>();
        for (Double key : tree.keySet()) {
            Vector<Integer> temp = tree.get(key);
            for (Integer aTemp : temp) {
                ans.add(aTemp);
            }
        }
        for (int i = 0; i < ans.size(); i++)
            p[i] = ans.get(i);
        return p;
    }

    public static void main(String[] args) {
        int[] a = RandomSample.randomsample(20,5);
        for (int anA : a) {
            System.out.print(anA + " ");
        }
    }
}
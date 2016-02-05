package em;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.FastMath;

public abstract class Util {
    public static String dataDirectory = "data";
    public static String resultsDirectory = "results";

    public static String getFilepath(String type,
                                     String locationType, String locationName, String fileExtension) {
        String fileSuffix = File.separator + locationType
                + File.separator + locationName + fileExtension;
        if(type == "data")
            return dataDirectory + fileSuffix;
        else if(type == "results")
            return resultsDirectory + fileSuffix;
        else
            return "";
    }

    public static String getResultsFilepath(String type,
                                            String locationType, String locationName, String fileExtension) {
        return resultsDirectory + File.separator + type + File.separator
                + locationType + File.separator + locationName + fileExtension;
    }

    public static String arrayToString(double[][] arr) {
        String s = "[";
        for(int i = 0; i < arr.length; i++) {
            s += "[";
            for(int j = 0; j < arr[0].length; j++) {
                s += Double.toString(arr[i][j]);
                if(j != arr[0].length - 1)
                    s += ", ";
            }
            s += "]";
            if(i != arr.length - 1)
                s += "\n";
        }
        return s + "]";
    }

    public static double[] doubleValues(Double[] arr) {
        double[] primArr = new double[arr.length];
        for(int i = 0; i < arr.length; i++)
            primArr[i] = arr[i].doubleValue();
        return primArr;
    }

    public static String matricesToString(RealMatrix[] arr) {
        String s = "";
        for(int i = 0; i < arr.length; i++) {
            s += arrayToString(arr[i].getData());
            s += "\n";
        }
        return s;
    }

    public static String matricesToString(ArrayList<RealMatrix> arr) {
        String s = "";
        for(int i = 0; i < arr.size(); i++) {
            s += arrayToString(arr.get(i).getData());
            s += "\n";
        }
        return s;
    }

    public static double[][] deepcopy(double[][] arr) {
        double[][] arrCopy = new double[arr.length][arr[0].length];
        for(int i = 0; i < arr.length; i++)
            System.arraycopy(arr[i], 0, arrCopy[i], 0, arr[0].length);
        return arrCopy;
    }

    public static RealMatrix[] deepcopy(RealMatrix[] arr) {
        RealMatrix[] arrCopy = new RealMatrix[arr.length];
        for(int i = 0; i < arr.length; i++)
            arrCopy[i] = arr[i].copy();
        return arrCopy;
    }

    public static ArrayList<Double[]> deepcopyArray(ArrayList<Double[]> arr) {
        ArrayList<Double[]> arrCopy = new ArrayList<Double[]>();
        for(int i = 0; i < arr.size(); i++) {
            arrCopy.add(new Double[arr.get(0).length]);
            System.arraycopy(arr.get(i), 0, arrCopy.get(i),
                    0, arr.get(0).length);
        }
        return arrCopy;
    }

    public static ArrayList<RealMatrix> deepcopyMatrix(ArrayList<RealMatrix> arr) {
        ArrayList<RealMatrix> arrCopy = new ArrayList<RealMatrix>();
        for(int i = 0; i < arr.size(); i++)
            arrCopy.add(arr.get(i).copy());
        return arrCopy;
    }

    public static double[][] stringToArray(String s) {
        String[] s1 = s.split("\n");
        ArrayList<ArrayList<Double>> dl = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < s1.length; i++) {
            dl.add(new ArrayList<Double>());
            for(String entry: s1[i].split(" ")) {
                dl.get(i).add(Double.parseDouble(entry));
            }
        }
        double[][] arr = new double[dl.size()][dl.get(0).size()];
        for(int r = 0; r < dl.size(); r++) {
            for(int c = 0; c < dl.get(0).size(); c++) {
                arr[r][c] = dl.get(r).get(c);
            }
        }
        return arr;
    }

    public static double[][] importFile(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(new File(filename));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return stringToArray(Charset.defaultCharset().decode(bb).toString());
        }
        finally {
            stream.close();
        }
    }

    public static void writeFile(String filepath, String text) throws IOException {
        PrintStream out = null;
        Path file = Paths.get(filepath);
        try {
            // Create the empty file with default permissions, etc.
            Files.createFile(file);
        } catch (FileAlreadyExistsException x) {

        } catch (IOException x) {
            // Some other sort of failure, such as permissions.
            System.err.format("createFile error: %s%n", x);
        }
        try {
            out = new PrintStream(new FileOutputStream(filepath));
            out.print(text);
        }
        finally {
            if (out != null) out.close();
        }
    }

    public static String exportFile(String filename, double[][] data) {
        File outFile = new File(filename);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(arrayToString(data));
        }
        catch (IOException e) {
            System.out.println("Could not write to " + filename);
        }
        finally {
            try {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e) {
                System.out.println("Could not close writer for " + filename);
            }
        }
        return filename;
    }

    public static double[] toArray(ArrayList<Double> al) {
        double[] arr = new double[al.size()];
        int i = 0;
        for(double d : al)
            arr[i++] = d;
        return arr;
    }

    /**
     * @param  p1 the first array
     * @param  p2 the second array
     * @return the distance between p1 and p2
     */
    public static double distance(double[][] p1, double[][] p2) {
        double diff = 0.0;
        for(int i = 0; i < p1.length; i++)
            diff += Util.distanceSquared(p1[i], p2[i]);
        return FastMath.pow(diff, 0.5);
    }

    /**
     * @param  p1 the first point
     * @param  p2 the second point
     * @return the Euclidean distance between p1 and p2
     */
    public static double distance(double[] p1, double[] p2) {
        double diff = 0.0;
        for(int i = 0; i < p1.length; i++)
            diff += FastMath.pow(p1[i] - p2[i], 2.0);
        return FastMath.pow(diff, 0.5);
    }

    /** @see #distance distance */
    public static double distance(Double[] p1, Double[] p2) {
        return distance(doubleValues(p1), doubleValues(p2));
    }

    /**
     * @param  p1 the first point
     * @param  p2 the second point
     * @return the square of the Euclidean distance between p1 and p2
     */
    public static double distanceSquared(double[] p1, double[] p2) {
        double sumSquares = 0;
        for(int i = 0; i < p1.length; i++)
            sumSquares += Math.pow((p1[i] - p2[i]), 2.0);
        return sumSquares;
    }

    /** @see #distanceSquared distanceSquared */
    public static double distanceSquared(double[] p1, Double[] p2) {
        return distanceSquared(p1, doubleValues(p2));
    }

    /**
     * @param point
     * @param mean the mean for the distribution
     * @param cov the covariance for the distribution
     * @return the probability the point belongs to the distribution defined by
     * the given mean and the given covariance
     */
    public static double probPoint(double[] point,
                                   double[] mean, RealMatrix cov) {
        MultivariateNormalDistribution dist =
                new MultivariateNormalDistribution(mean, cov.getData());
        return dist.density(point);
    }

    /**
     * A non-symmetric measure of the difference between two probability 
     * distributions.
     * @return information lost when Q is used to approximate P
     * @see <a href="http://en.wikipedia.org/wiki/Kullback�Leibler_divergence#Definition">Kullback�Leibler divergence</a>
     */
    private static double KullbackLeiblerDivergence(double[] P, double[] Q) {
        assert(P.length == Q.length);
        double D = 0.0;
        for(int i = 0; i< P.length; i++)
            D += FastMath.log(P[i] / Q[i]) * P[i];
        return D;
    }

    /**
     * Implementation of the Jensen-Shannon divergence test, which measures the
     * similarity between two probability distributions. Based on the
     * Kullback- Leibler divergence.
     * @return total divergence to the average
     * @see #KullbackLeiblerDivergence(double[], double[]) KullbackLeiblerDivergence
     * @see <a href="http://en.wikipedia.org/wiki/Jensen%E2%80%93Shannon_divergence#Definition">Jensen�Shannon divergence</a>
     */
    public static double JensenShannonDivergence(double[] P, double[] Q) {
        assert(P.length == Q.length);
        double[] M = new double[P.length];
        for(int i = 0; i< P.length; i++)
            M[i] = (P[i] + Q[i]) / 2.0;
        return KullbackLeiblerDivergence(P, M) / 2.0
                + KullbackLeiblerDivergence(Q, M) / 2.0;
    }

    /**
     * Implementation of Mahalanobis Distance.
     * @param mean mean of distribution
     * @param cov covariance matrix of distribution
     * @param point point finding distance from distribution of
     */
    public static double MahalanobisDistance(double[] mean, RealMatrix cov, double[] point) {
        assert(mean.length == cov.getData().length && mean.length == point.length);
        double [] diff = new double[mean.length];
        for(int i = 0; i < diff.length; i++)
            diff[i] = point[i] - mean[i];
        Array2DRowRealMatrix v = new Array2DRowRealMatrix(diff);
        return FastMath.sqrt((v.transpose()).multiply(cov.multiply(v)).getEntry(0,0));
    }
    public static double MahalanobisDistance(Double[] mean, RealMatrix cov, Double[] point) {
        return MahalanobisDistance(doubleValues(mean), cov, doubleValues(point));
    }
    public static double MahalanobisDistance(Double[] mean, RealMatrix cov, double[] point) {
        return MahalanobisDistance(doubleValues(mean), cov, point);
    }

    /**
     * Checks if the given matrix is positive semi-definite and non-singular.
     * @param M covariance matrix to check
     * @return true iff M is positive semi-definite and non-singular
     */
    public static boolean isValidCovarianceMatrix(RealMatrix M) {
        try {
            EigenDecomposition ed = new EigenDecomposition(M);
            double[] realEigen = ed.getRealEigenvalues();
            double[] imagEigen = ed.getImagEigenvalues();
            for(int i = 0; i < realEigen.length; i++)
                if(realEigen[i] < 0 || (realEigen[i] == 0 && imagEigen[i] == 0))
                    return false;
            return true;
        }
        catch(MaxCountExceededException | MathArithmeticException ex) {
            return false;
        }

    }
}
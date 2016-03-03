package cluster.em.algo;

import cluster.em.base.BaseExpectationMaximization;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Ordering;
import common.utils.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;

/**
 * Created by Pankajan on 25/02/2016.
 */
public class PerClusterMOG implements BaseExpectationMaximization {
    private static final boolean HOME = false;
    private static final String VARIABLE_FILE_NAME = "50_occurances.csv";

    private static final int CLUSTER_COUNT = 12;
    private static final int COMPONENT_COUNT = 3;
    private static final int MAX_ITERATION = 1000;
    public static final int SAMPLE_SIZE = 1000;
    private static final int MAX_COMPONENTS = 20;

    private static final double COMPONENT_PROBABILITY_THRESHOLD = -10;
    public static final double LOG_LIKELIHOOD_DIFF = 0.001;
    public static final int LOG_LIKELIHOOD_THRESHOLD = -5;

    private MinMaxPriorityQueue<Double> minimumValues = MinMaxPriorityQueue.expectedSize(10000).maximumSize(10000).create();
    private MinMaxPriorityQueue<Double> maximumValues = MinMaxPriorityQueue.orderedBy(Ordering.natural().reverse()).expectedSize(10000).maximumSize(10000).create();
    private Double[] minValuesArray;
    private Double[] maxValuesArray;

    //Map with variable names as key and their values as value. Ex: background_tokenizer.js->2481-3510_currentLine -> {10.11, 11, 100, 12.99}
    private Map<String, List<Double>> variableValues;
    private HashMap<String, String> variableProjectMap;
    //Map with variable names as key and their respective clusters as values. Ex: background_tokenizer.js->2481-3510_currentLine -> 1
    private Map<String, Integer> variableClusters = new HashMap<>();
    private Map<String, String> variableComponents = new HashMap<>();
    //Map with variable cluster_component as key and all the variable values that are assigned to that component as value. Ex: 1_2-> {12, 23, 34, 12.01}
    private Map<String, List<Double>> clusterComponentVariableValues = new HashMap<>();


    //current cluster count
    private int currentClusterCount = CLUSTER_COUNT;
    //Map with cluster number as key and their respective current cluster component count as value
    private Map<Integer, Integer> currentComponentCount = new HashMap<>();
    //Map with cluster_component as key and their respective gaussian parameters (mean, sd) as value. Ex: 1_2 -> [12.34, 1.89]
    private Map<String, double[]> currentGaussianParameters = new HashMap<>();
    //Map with cluster or cluster_component as key and their prior probability as value. Ex1: 1 -> 0.0344 Ex2: 1_2 -> 0.0023
    private Map<String, Double> currentClusterComponentPriors = new HashMap<>();
    //Array with variable count in each cluster (0 index referes cluster number 1)
    private int[] clusterVariableCount = new int[currentClusterCount];

    private int currentIteration = 1;
    private double minimum_value = Double.MAX_VALUE;
    private double maximum_value = Double.MIN_VALUE;
    private List<String> variableNameList;
    private double previosSumLogLikelihood = 0;
    private boolean isConverged = false;

    //    private BufferedWriter writer;
    public void execute() throws IOException {
        /**
         * Read variable names from the input file and save them in a Map with project as keys and variable name list as value
         */
        try {
            variableNameList = new ArrayList<>();
            variableProjectMap = new HashMap<>();
            if(HOME) {
                List<String> variableNames = readVariableNameFile();
                variableValues = loadVariableValues(variableNames);
//                writer = new BufferedWriter(new FileWriter(Constants.RESULT_ROOT+"result.txt"));
            } else {
                variableValues = loadVariableValues(new File(Constants.RESULT_OFFICE_ROOT + Constants.FILTERED_VARIABLE_LIST_FOLDER + Constants.EACH_VARIABLE_FOLDER));
//                writer = new BufferedWriter(new FileWriter(Constants.RESULT_OFFICE_ROOT+"result.txt"));
            }
            filterVariables();
            initializeEM(CLUSTER_COUNT, COMPONENT_COUNT);
            executeEMAlgorithm();

        } catch (IOException e) {
            System.err.println("Error reading variable names file with Exception :" + e.getLocalizedMessage());
        }
    }

    private void filterVariables() {
        Iterator<Map.Entry<String, List<Double>>> iterator = variableValues.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Double>> entry = iterator.next();
            HashSet<Double> set = new HashSet<>(entry.getValue());
            if (set.size() > 200) {
                variableNameList.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    private void executeEMAlgorithm() throws IOException {
        while (!isConverged) {
            executeEStep();
            executeMStep();
        }
    }

    private void printResults() throws IOException {
        for (int k = 1; k <= currentClusterCount; k++) {
            System.out.println();
            System.out.println("                              Cluster " + k + " with variables  " + clusterVariableCount[k-1]);
            for (int i=1; i<=currentComponentCount.get(k); i++) {
                double[] params = currentGaussianParameters.get(k + "_" + i);
                System.out.print("           Component " + i);
                System.out.println(" -> Mean : " + params[0] + " , SD : " + params[1]);
            }
           /* for (Map.Entry<String, Integer> variableCluster : variableClusters.entrySet()) {
                if (variableCluster.getValue() == k) {
                    String[] components = variableComponents.get(variableCluster.getKey()).split(",");
                    int[] componentCount = new int[currentComponentCount.get(variableCluster.getValue())];
                    for (String component : components) {
                        componentCount[Integer.parseInt(component) - 1]++;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i1 = 0; i1 < componentCount.length; i1++) {
                        int i = componentCount[i1];
                        builder.append((i1 + 1)).append(" x ").append(i).append(" | ");
                    }

                    System.out.println(variableProjectMap.get(variableCluster.getKey()) + " -> " + variableCluster.getKey() + " : " + builder);
                }
            }*/
        }
//        writer.flush();
//        writer.close();
        }

    private void initializeEM(int clusterCount, int componentCount) {
        currentClusterCount = clusterCount;
        double clusterPrior = 1.0 / clusterCount;
        double componentPrior = 1.0 / componentCount;

        minValuesArray = minimumValues.toArray(new Double[minimumValues.size()]);
        maxValuesArray = maximumValues.toArray(new Double[maximumValues.size()]);
        for (int i = 1; i <= clusterCount; i++) {
            currentComponentCount.put(i, componentCount);
            currentClusterComponentPriors.put(String.valueOf(i), Math.log(clusterPrior));
            double[] randomSamples = getRandomSamples();
            int range = randomSamples.length/componentCount;
            int start = 0;
            for (int j = 1; j <= componentCount; j++) {
                DescriptiveStatistics descriptiveStatistics;
                do {
                    descriptiveStatistics = new DescriptiveStatistics(Arrays.copyOfRange(randomSamples, start, start + range));
                    currentGaussianParameters.put(i + "_" + j, new double[]{descriptiveStatistics.getMean(), descriptiveStatistics.getStandardDeviation()});
                    currentClusterComponentPriors.put(i + "_" + j, Math.log(componentPrior));
                } while (Double.isNaN(descriptiveStatistics.getMean()) || Double.isNaN(descriptiveStatistics.getStandardDeviation()));
                start += range;
            }
        }
    }

    private double[] getRandomSamples() {
        double[] randomSamples;
       /* Random random = new Random();
        int nextInt = random.nextInt(5);
        if (nextInt == 1) {
            randomSamples = getRandomSamples(minValuesArray, SAMPLE_SIZE);
        } else if (nextInt == 2) {
            randomSamples = getRandomSamples(maxValuesArray, SAMPLE_SIZE);
        } else {
            randomSamples = getRandomSamples(SAMPLE_SIZE, true);
        }*/

        randomSamples = getRandomSamples(45, false);

        return randomSamples;
    }

    private double[] getRandomSamples(Double[] minValuesArray, int sampleSize) {
        double[] values = new double[sampleSize];
        Random random = new Random();
        for (int i = 0; i < sampleSize; i++) {
            values[i] = minValuesArray[random.nextInt(minValuesArray.length)];
        }
        return values;
    }

    private double[] getTopOrBottomValues(MinMaxPriorityQueue<Double> minimumValues, boolean top, int sampleSize) {
        double[] values = new double[sampleSize];
        if (top) {
            for (int i = 0; i < sampleSize; i++) {
                values[i] = minimumValues.removeFirst();
            }
        } else {
            for (int i = 0; i < sampleSize; i++) {
                values[i] = minimumValues.removeLast();
            }
        }
        return values;
    }

    private double[] getRandomSamples(int noOfSamples, boolean randomValuesFromDifferentVariables) {
        double[] randomSampleValues = new double[noOfSamples];
        if(randomValuesFromDifferentVariables) {
            for (int i = 0; i < noOfSamples; i++) {
                int variableCountBound = variableNameList.size();
                Random ran = new Random();
                List<Double> variableValues = this.variableValues.get(variableNameList.get(ran.nextInt(variableCountBound)));
                randomSampleValues[i] = variableValues.get(ran.nextInt(variableValues.size()));
            }
        } else {
            int variableCountBound = variableNameList.size();
            Random ran = new Random();
            List<Double> variableValues;
            int size;
            do {
                variableValues = this.variableValues.get(variableNameList.get(ran.nextInt(variableCountBound)));
                size = variableValues.size();
            } while (variableValues.size() >= noOfSamples);
            for (int i = 0; i < noOfSamples; i++) {
                randomSampleValues[i] = variableValues.get(ran.nextInt(size));
            }
        }
        return randomSampleValues;
    }

    private Map<String, List<Double>> loadVariableValues(File root) throws IOException {
        Map<String, List<Double>> variableValueMap = new HashMap<>();
        System.out.println("Loading ROOT Folder "+ root.getAbsolutePath());
        for (File folder : root.listFiles()) {
            if(folder.getName().startsWith(".")) continue;
            System.out.println("Loading folder " + folder.getAbsolutePath());
            for (File file : folder.listFiles()) {
                if(file.getName().startsWith(".")) continue;
                System.out.println("Loading file " + file.getAbsolutePath());
                readFile(variableValueMap, file.getAbsolutePath());
            }
        }
        return variableValueMap;
    }

    private Map<String, List<Double>> loadVariableValues(List<String> variableNames) throws IOException {
        Map<String, List<Double>> variableValueMap = new HashMap<>();
        for (String variableName : variableNames) {
            readFile(variableValueMap, Constants.RESULT_ROOT + Constants.EACH_VARIABLE_FOLDER + variableName + ".csv");
        }
        return variableValueMap;
    }

    private void readFile(Map<String, List<Double>> variableValueMap, String fileName) throws IOException {
        BufferedReader variableReader = getBufferedReader(fileName);
        String value;
        List<Double> variableValues = new ArrayList<>();
        while ((value = variableReader.readLine()) != null) {
            double v = Double.parseDouble(value);
            if (!Double.isNaN(v) && Double.isFinite(v)) {
                if (v < minimum_value) minimum_value = v;
                if (v > maximum_value) maximum_value = v;
                variableValues.add(v);
                minimumValues.add(v);
                maximumValues.add(v);
            }
        }

        String[] split = fileName.split("/");
        String variableName = split[split.length - 1].split(".csv")[0];
        String projectName = split[split.length - 2];
        variableValueMap.put(variableName, variableValues);
        variableNameList.add(variableName);
        variableProjectMap.put(variableName, projectName);
    }

    private List<String> readVariableNameFile() throws IOException {
        BufferedReader reader = getBufferedReader(Constants.RESULT_ROOT + Constants.FILTERED_VARIABLE_LIST_FOLDER + VARIABLE_FILE_NAME);
        List<String> variableNames = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            variableNames.add(line.replace(",", "/"));
        }
        return variableNames;
    }

    private BufferedReader getBufferedReader(String fileName) throws FileNotFoundException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(fileName));
        return reader;
    }

    public void executeEStep() throws IOException {
        variableClusters = new HashMap<>();
        variableComponents = new HashMap<>();
        clusterVariableCount = new int[currentClusterCount];
        double sumLogLikelihood = 0;
        for (Map.Entry<String, List<Double>> variable : variableValues.entrySet()) {
//            System.out.println("Variable : " + variable.getKey());
            double sumLogProbability = Double.NEGATIVE_INFINITY;
            int selectedCluster = -1;
            Map<Double, Integer> selectedComponents = new HashMap<>();
            for (int i = 1; i <= currentClusterCount; i++) {
                Map<Double, Integer> tempComponents = new HashMap<>();
                double currentSumLogProbability = getProbabilityForSingleCluster(variable, i, tempComponents, false);
                if (currentSumLogProbability > sumLogProbability) {
                    sumLogProbability = currentSumLogProbability;
                    selectedCluster = i;
                    selectedComponents = tempComponents;
                }
            }


            if (selectedCluster != -1) {
                if(sumLogProbability < COMPONENT_PROBABILITY_THRESHOLD && currentComponentCount.get(selectedCluster) < MAX_COMPONENTS) {
                    Map<Double, Integer> tempComponents = new HashMap<>();
                    sumLogProbability = getProbabilityForSingleCluster(variable, selectedCluster, tempComponents, true);
                }


                sumLogLikelihood += sumLogProbability;
                variableClusters.put(variable.getKey(), selectedCluster);
                clusterVariableCount[selectedCluster - 1]++;
                for (Map.Entry<Double, Integer> entry : selectedComponents.entrySet()) {
                    if (entry.getValue() != -1) {
                        String cluster_component = selectedCluster + "_" + entry.getValue();
                        if (!clusterComponentVariableValues.containsKey(cluster_component))
                            clusterComponentVariableValues.put(cluster_component, new ArrayList<>());
                        clusterComponentVariableValues.get(cluster_component).add(entry.getKey());
                        if (!variableComponents.containsKey(variable.getKey()))
                            variableComponents.put(variable.getKey(), String.valueOf(entry.getValue()));
                        else
                            variableComponents.put(variable.getKey(), variableComponents.get(variable.getKey()) + "," + entry.getValue());
                    }
                }
            }
//            else {
//                System.out.println("Error");
//            }
        }
        sumLogLikelihood /= variableValues.size();
        if (currentIteration == MAX_ITERATION || Math.abs(previosSumLogLikelihood - sumLogLikelihood) <= LOG_LIKELIHOOD_DIFF || sumLogLikelihood >= LOG_LIKELIHOOD_THRESHOLD) {
            System.out.println("Algorithm converged with iteration " + currentIteration + " loglikelihood different " + Math.abs(previosSumLogLikelihood - sumLogLikelihood) + " and current average log likelihood " + sumLogLikelihood);
            isConverged = true;
        }
        previosSumLogLikelihood = sumLogLikelihood;
        System.out.println("Log-Likelihood : " + sumLogLikelihood);
    }

    private double getProbabilityForSingleCluster(Map.Entry<String, List<Double>> variable, int i, Map<Double, Integer> tempComponents, boolean addComponents) {
        Integer componentCount = currentComponentCount.get(i);
        double currentSumLogProbability = 0;
        int uniqueVariableValues = 0;
        for (Double variableValue : variable.getValue()) {
            if (tempComponents.containsKey(variableValue)) {
                continue;
            }
            uniqueVariableValues++;
            int component = -1;
            double valueProbability = Double.NEGATIVE_INFINITY;

            for (int j = 1; j <= componentCount; j++) {
                double[] componentParameters = currentGaussianParameters.get(i + "_" + j);
                NormalDistribution normalDistribution = new NormalDistribution(componentParameters[0], componentParameters[1]);
                if (new Double(normalDistribution.logDensity(variableValue)).isInfinite()) continue;
                double logProbability = normalDistribution.logDensity(variableValue);
//                                + currentClusterComponentPriors.get(String.valueOf(i))
//                                + currentClusterComponentPriors.get(i + "_" + j);
//                        if (logProbability == Double.NEGATIVE_INFINITY) {
//                            System.out.println("ERROR");
//                        }
                if (logProbability > valueProbability) {
                    valueProbability = logProbability;
                    component = j;
                }
            }

            if(addComponents && valueProbability < COMPONENT_PROBABILITY_THRESHOLD && componentCount < MAX_COMPONENTS) {
//                System.out.println("VALUE PROBABILITY :  " + valueProbability + " AND COMPONENT COUNT :  " + (componentCount+1));
                componentCount++;
                currentComponentCount.put(i, componentCount);
                currentGaussianParameters.put(i + "_" + componentCount, new double[]{variableValue, 1});
            } else {
                tempComponents.put(variableValue, component);
                currentSumLogProbability += valueProbability;
            }

        }
        return currentSumLogProbability/uniqueVariableValues;
    }

    public void executeMStep() throws IOException {
//        currentGaussianParameters = new HashMap<>();
        currentClusterComponentPriors = new HashMap<>();
        int totalVariableCount = variableValues.size() + currentClusterCount;
        for (int i = 1; i <= currentClusterCount; i++) {
            currentClusterComponentPriors.put(String.valueOf(i), Math.log(((double) (clusterVariableCount[i - 1] + 1)) / totalVariableCount));
            Integer clusterComponentCount = currentComponentCount.get(i);
            double clusterVariableValuesTotal = clusterComponentCount;
            for (int j = 1; j <= clusterComponentCount; j++) {
                List<Double> values = clusterComponentVariableValues.get(i + "_" + j);
                if (values != null) clusterVariableValuesTotal += values.size();
            }

            List<Integer> emptyComponents = new ArrayList<>();
            for (int j = 1; j <= clusterComponentCount; j++) {
                List<Double> values = clusterComponentVariableValues.get(i + "_" + j);
                if (values != null && values.size() > 1) {
                    double[] valuesInPrimitive = ArrayUtils.toPrimitive(values.toArray(new Double[values.size()]));
                    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(valuesInPrimitive);
/*
                    if (Double.isNaN(descriptiveStatistics.getStandardDeviation()) || descriptiveStatistics.getStandardDeviation() <= 0) {
                            double[] randomSamples = getRandomSamples();
                            descriptiveStatistics = new DescriptiveStatistics(randomSamples);
                            currentGaussianParameters.put(i + "_" + j, new double[]{descriptiveStatistics.getMean(), descriptiveStatistics.getStandardDeviation()});
                    }*/
                    currentGaussianParameters.put(i + "_" + j, new double[]{descriptiveStatistics.getMean(), descriptiveStatistics.getStandardDeviation()});

                    currentClusterComponentPriors.put(i + "_" + j, Math.log((values.size() + 1) / clusterVariableValuesTotal));
                } else if (values == null) {
                    emptyComponents.add(j);
                } else {
                    currentGaussianParameters.put(i + "_" + j, new double[]{values.get(0), 1});
                    currentClusterComponentPriors.put(i + "_" + j, Math.log(2 / clusterVariableValuesTotal));
                }
            }

            //Remove empty components
            if (emptyComponents.size() == clusterComponentCount) {
                for (int j = 2; j <= clusterComponentCount; j++) {
                    currentGaussianParameters.remove(i + "_" + j);
                    currentClusterComponentPriors.remove(i + "_" + j);
                    clusterComponentVariableValues.remove(i + "_" + j);
                    currentComponentCount.put(i, (currentComponentCount.get(i) - 1));
                }
            } else {
            int currentSwitchingComponent = clusterComponentCount;
            for (int j = 1; j <= clusterComponentCount; j++) {
                if (emptyComponents.contains(j)) {
                    while (emptyComponents.contains(currentSwitchingComponent) && currentSwitchingComponent > 0)
                        currentSwitchingComponent--;

                    if (currentSwitchingComponent > j) {
                        //Switch
                        currentGaussianParameters.put(i + "_" + j, currentGaussianParameters.get(i + "_" + currentSwitchingComponent));
                        currentClusterComponentPriors.put(i + "_" + j, currentClusterComponentPriors.get(i + "_" + currentSwitchingComponent));
                        clusterComponentVariableValues.put(i + "_" + j, clusterComponentVariableValues.get(i + "_" + currentSwitchingComponent));
                        currentComponentCount.put(i, (currentComponentCount.get(i) - 1));
                        currentGaussianParameters.remove(i + "_" + currentSwitchingComponent);
                        currentClusterComponentPriors.remove(i + "_" + currentSwitchingComponent);
                        clusterComponentVariableValues.remove(i + "_" + currentSwitchingComponent);

                        for (Map.Entry<String, Integer> variableCluster : variableClusters.entrySet()) {
                            if (variableCluster.getValue() == i) {
                                StringBuilder builder = new StringBuilder();
                                String[] components = variableComponents.get(variableCluster.getKey()).split(",");
                                for (String component : components) {
                                    if (Integer.parseInt(component) == currentSwitchingComponent)
                                        builder.append("," + j);
                                    else builder.append(",").append(component);
                                }
                                variableComponents.put(variableCluster.getKey(), builder.substring(1));
                            }
                        }
                        currentSwitchingComponent--;
                    } else {
                        if (currentComponentCount.get(i) != 1) {
                            //Remove
                            currentGaussianParameters.remove(i + "_" + j);
                            currentClusterComponentPriors.remove(i + "_" + j);
                            clusterComponentVariableValues.remove(i + "_" + j);
                            currentComponentCount.put(i, (currentComponentCount.get(i) - 1));
                        }
                    }
                }
            }
        }
        }


        clusterComponentVariableValues = new HashMap<>();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>  " + currentIteration++ + "  <<<<<<<<<<<<<<<<<<<<<<<<");
        printResults();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<");
    }

    public static void main(String[] args) throws IOException {
        PerClusterMOG clusterMOG = new PerClusterMOG();
        clusterMOG.execute();
    }
}

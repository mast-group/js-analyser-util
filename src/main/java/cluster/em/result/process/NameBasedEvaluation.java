package cluster.em.result.process;

import common.utils.Constants;
import common.utils.Levenshtein;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Pankajan on 10/03/2016.
 */
public class NameBasedEvaluation {
    String mathjsFile = "EM-Cluster-MathJS-3_PreProcessed.txt";
    String allFile =  "EM-Cluster-All-Projects_PreProcessed.txt";
    final String fileName = Constants.RESULT_ROOT + Constants.EM_CLUSTER_FOLDER +  mathjsFile;
    List<List<String>> clusterListWithVariables;
    Map<String, List<Integer>> variablesCluster = new HashMap<>();

    double precision;
    double recall;

    public void execute() throws IOException {
        loadFile(fileName);
        calculatePrecision();
        calculateRecall();
        printResults();
    }

    private void loadFile(String fileName) throws IOException {
        clusterListWithVariables = new ArrayList<>();
        List<String> currentClusterList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        int cluster=1;
        while((line = reader.readLine()) != null) {
            if(line.startsWith(">>>>>>>")) {
                cluster++;
                clusterListWithVariables.add(currentClusterList);
                currentClusterList = new ArrayList<>();
            } else {
                String projectAndVariableName = line.split(":")[0].trim();
                String variableName = projectAndVariableName.substring(projectAndVariableName.lastIndexOf("_") + 1);
                currentClusterList.add(variableName);
                if(!variablesCluster.containsKey(variableName)) variablesCluster.put(variableName, new ArrayList<Integer>());
                variablesCluster.get(variableName).add(cluster);
            }
        }
    }

    private void calculatePrecision() {
        double precision = 0;
        int clusterCount = 0;
        for (List<String> variableList : clusterListWithVariables) {
            double equalNameCount = 0;
            int size = variableList.size();
            if(size>1) {
                for (int i = 0; i < size; i++) {
                    for (int j = i + 1; j < size; j++) {
                        equalNameCount += new Levenshtein().compare(variableList.get(i), variableList.get(j));
//                        if (variableList.get(i).equals(variableList.get(j))) equalNameCount++;
                    }
                }
                precision += equalNameCount / ((size * (size - 1)) / 2.0);
                clusterCount++;
            }
        }
        this.precision = precision/clusterCount;
    }

    private void calculateRecall() {
        int totalNameSimilarity = 0;
        double totalNameAndClusterSimilarity = 0;

        for (Map.Entry<String, List<Integer>> variableCluster : variablesCluster.entrySet()) {
            List<Integer> clusterList = variableCluster.getValue();
            int size = clusterList.size();
            Collections.sort(clusterList, Comparator.naturalOrder());
            int currentClusterNumber = 1;
            int clusterCount = 0;
            for (Integer clusterNumber : clusterList) {
                if(clusterNumber!=currentClusterNumber) {
                    currentClusterNumber = clusterNumber;
                    totalNameAndClusterSimilarity += ((clusterCount*(clusterCount-1))/2);
                    clusterCount = 1;
                } else {
                    clusterCount++;
                }
            }
            totalNameAndClusterSimilarity += ((clusterCount*(clusterCount-1))/2);
            totalNameSimilarity += ((size*(size-1))/2);
        }
        this.recall =  totalNameAndClusterSimilarity/totalNameSimilarity;
    }


    private void printResults() {
        DecimalFormat f = new DecimalFormat("##.00");
        System.out.println("Processed File " +fileName );
        System.out.println();
        System.out.println("Precision\t:\t" + f.format(precision*100) + "%");
        System.out.println("Recall\t\t:\t" + f.format(recall*100) + "%");
    }

    public static void main(String[] args) throws IOException {
        NameBasedEvaluation nameBasedEvaluation = new NameBasedEvaluation();
        nameBasedEvaluation.execute();
    }
}

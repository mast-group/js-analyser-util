package variable.summary;

import common.utils.Constants;

import java.io.*;
import java.nio.file.Files;

/**
 * 1. Util methods to save each variables into each values from a given project.
 *    Each project will create one folder and inside that each variable will get one file
 *    Values of that variable will get printed line by line
 * 2. Util methods to summarize variable names and values in R specific format
 *    Two columns with first column: name and second column value
 *      Ex: file->method->variableName, 10.121212
 * Created by Pankajan on 29/01/2016.
 */
public class VariableValuesIntoFiles {

    /**
     * Util method to save each variable from one given project/projects in one folder into separate files
     * One directory will be created in the ROOT folder in the given project name
     * Each variable will create one new file with the name of the variable
     * Each value of the variable will be printed in the file in each line
     * @param path
     * @throws IOException
     */
    public void saveEachVariableValuesInFiles(String path)  throws IOException {
        File file = new File(path);

        if(file.isDirectory()) {
            for (File resultSummary : file.listFiles()) {
                saveOneProjectVariablesIntoFiles(path, resultSummary);
            }
        } else {
            saveOneProjectVariablesIntoFiles(path, file);
        }
    }

    /**
     * Util method to save each variable from one given project into separate files
     * One directory will be created in the ROOT folder in the given project name
     * Each variable will create one new file with the name of the variable
     * Each value of the variable will be printed in the file in each line
     * @param path
     * @param resultSummary
     * @throws IOException
     */
    private void saveOneProjectVariablesIntoFiles(String path, File resultSummary) throws IOException {
        if (resultSummary.getName().contains("_summary_")) {
            BufferedReader reader = new BufferedReader(new FileReader(resultSummary));

            File resultFolder = new File(path.split("/summary/")[0] + "/each_variable_file/" + resultSummary.getName().substring(4, resultSummary.getName().indexOf(".txt_summary_")));
            resultFolder.mkdir();

            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] lineContent = line.split(",");
                PrintWriter writer = new PrintWriter(resultFolder.getAbsolutePath() + "/" + lineContent[0].substring(lineContent[0].lastIndexOf("/")) + "_" + lineContent[2] + ".csv", "UTF-8");

                for (int i = 5; i < lineContent.length; i++) {
                    String[] values = lineContent[i].split(" x ");
                    for (int j = 0; j < Integer.parseInt(values[1]); j++) {
                        writer.println(values[0]);
                    }
                }
                writer.flush();
                writer.close();

            }

        }
    }



    /**
     * Save variables from one project/many projects in single file with variable name in one column and value in another
     * @param path
     * @throws IOException
     */
    public void saveVariableValuesInSingleFile(String path, int thresholdUniqueValues, int thresholdTotalValues) throws IOException {
        File file = new File(path);
        if(file.isDirectory()) {
            for (File resultSummary : file.listFiles()) {
                if (resultSummary.getName().contains("_summary_")) {
                    saveVariableValuesOfOneProjectInSingleFile(resultSummary, thresholdUniqueValues, thresholdTotalValues);
                }
            }
        } else {
            saveVariableValuesOfOneProjectInSingleFile(file, thresholdUniqueValues, thresholdTotalValues);
        }
    }

    /**
     * Save variables from one project in single file with variable name in one column and value in another
     * @param file
     * @throws IOException
     */
    private void saveVariableValuesOfOneProjectInSingleFile(File file, int thresholdUniqueValues, int thresholdTotalValues) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        File resultFile = new File(file.getParent() + "/" + file.getName().substring(4, file.getName().indexOf(".txt_summary_")) + "_variable_summary.csv");
        Files.deleteIfExists(resultFile.toPath());

        PrintWriter writer = new PrintWriter(resultFile, "UTF-8");

        String line;
        reader.readLine();
        while((line = reader.readLine()) != null) {
            String[] lineContent = line.split(",");
            String variableName = lineContent[0].substring(lineContent[0].lastIndexOf("/")+1) + "_" + lineContent[2];
            if((Integer.parseInt(lineContent[3]) == -1 || Integer.parseInt(lineContent[3]) >= thresholdUniqueValues) &&
                    (Integer.parseInt(lineContent[4]) == -1 || Integer.parseInt(lineContent[4]) >= thresholdTotalValues)) {
                for (int i = 5; i < lineContent.length; i++) {
                    String[] values = lineContent[i].split(" x ");
                    for (int j = 0; j < Integer.parseInt(values[1]); j++) {
                        writer.println(variableName + "," + values[0]);
                    }
                }
            }
        }
        writer.flush();
        writer.close();
    }

    static final String ROOT = "/Users/Pankajan/Edinburgh/Research_Source/Result/summary/log_pomelo.txt_summary_results.txt";

    public static void main(String[] args) throws IOException {
        String fileName = "log_" + Constants.LESS + ".txt_summary_results.txt";

        VariableValuesIntoFiles intoFiles = new VariableValuesIntoFiles();
        intoFiles.saveEachVariableValuesInFiles(ROOT);
//        intoFiles.saveVariableValuesInSingleFile(ROOT+fileName, 10, 100);

    }
}

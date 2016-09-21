package variable.summary;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
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
     * @param outputPath
     * @throws IOException
     */
    public void saveEachVariableValuesInFiles(String path, String outputPath, int thresholdUniqueValues, int thresholdTotalValues)  throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        File resultFolder = new File(outputPath);
        if(!resultFolder.exists())
            resultFolder.mkdir();

        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] lineContent = line.split(",");
            int uniqueValues = lineContent.length - 5;
            int totalTimes = 0;
            for (int i = 5; i < lineContent.length; i++) {
                String[] values = lineContent[i].split(" x ");
                totalTimes += Integer.parseInt(values[1]);
            }

            if (uniqueValues >= thresholdUniqueValues && totalTimes >= thresholdTotalValues) {
                PrintWriter writer = new PrintWriter(resultFolder.getAbsolutePath() + "/" + lineContent[0] + "_" + lineContent[2] + ".csv", "UTF-8");


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
     * Save variables in single file with variable name in one column and value in another
     * @param path
     * @throws IOException
     */
    public void saveVariableValuesInSingleFile(String path, String outputPath, int thresholdUniqueValues, int thresholdTotalValues) throws IOException {
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        File resultFile = new File(outputPath);
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
                    try {
                        int valueInt = Integer.parseInt(values[1]);
                        for (int j = 0; j < valueInt; j++) {
                            writer.println(variableName + "," + values[0]);
                        }
                    } catch(NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}
                }
            }
        }
        writer.flush();
        writer.close();    }


    public static void main(String[] args) throws IOException {
        final Parameters params = new Parameters();
        final JCommander jc = new JCommander(params);

        try {
            jc.parse(args);
            File outFile = new File(params.output);
            if(params.quartiles) {
                if (!outFile.exists() && (new File(outFile.getParent())).exists()) {
                    VariableQuartiles intoFiles = new VariableQuartiles();
                    intoFiles.saveVariableQuartilesInSingleFile(params.input, params.output, params.unique, params.total);
                } else {
                    System.out.println("Error: Output file already exists or parent folder not available");
                }
            } else {
                VariableValuesIntoFiles intoFiles = new VariableValuesIntoFiles();
                if (params.singleFile) {
                    if (!outFile.exists() && (new File(outFile.getParent())).exists()) {
                        intoFiles.saveVariableValuesInSingleFile(params.input, params.output, params.unique, params.total);
                    } else {
                        System.out.println("Error: Output file already exists or parent folder not available");
                    }
                } else {
                    if (outFile.isDirectory() || (!outFile.exists() && (new File(outFile.getParent())).exists())) {
                        intoFiles.saveEachVariableValuesInFiles(params.input, params.output, params.unique, params.total);
                    } else {
                        System.out.println("Error: Output path is not a folder or parent folder not available");
                    }
                }
            }
        } catch (final ParameterException e) {
            System.out.println(e.getMessage());
            jc.usage();
        }


    }

    /** Command line parameters */
    public static class Parameters {

        @Parameter(names = { "-i", "--input" }, description = "Input path of summary file", required = true)
        String input;

        @Parameter(names = { "-o", "--output" },
                description = "Output path of result. Valid folder to save each variable in seperate file. Valid text file to save all variables in single file",
                required = true)
        String output;

        @Parameter(names = { "-u", "--unique" }, description = "Threshold to select variables with minimum unique values")
        int  unique = 1;

        @Parameter(names = { "-t", "--total" }, description = "Threshold to select variables with minimum total values")
        int total = 50;

        @Parameter(names = { "-s", "--single" }, description = "Include to save all variables in singe file. Default is to save each variable in seperate file")
        boolean singleFile = false;

        @Parameter(names = { "-q", "--quartile" }, description = "Include to save quartiles of each variable in a single file rather than every value")
        boolean quartiles = false;
    }
}

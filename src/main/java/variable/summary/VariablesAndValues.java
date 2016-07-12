package variable.summary;

import common.utils.Constants;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by Pankajan on 23/01/2016.
 */
public class VariablesAndValues {

    public static void main(String[] args) throws IOException {
        /*for (String project : Constants.CURRENT_PROJECTS) {
            String fileName = "/Users/Pankajan/Edinburgh/Research_Source/Result/raw/log_" + project;
            variable.summary.VariablesAndValues variablesAndValues = new variable.summary.VariablesAndValues();
            variablesAndValues.analyze(fileName+ ".txt");
        }*/

        List<String> selectedProjects = new ArrayList<String> (Arrays.asList(new String [] {"log_mathjs.txt"}));
        boolean ONLY_SELECTED_PROJECTS = true;
        File folder = new File(Constants.RESULT_ROOT + Constants.RAW_FOLDER);
        for (File file : folder.listFiles()) {
            if(!file.isDirectory() && selectedProjects.contains(file.getName()) && ONLY_SELECTED_PROJECTS){
                VariablesAndValues variablesAndValues = new VariablesAndValues();
                variablesAndValues.analyze(file.getAbsolutePath());
            }
        }
    }

    private void analyze(String fileName) throws IOException {
        Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();
        Map<String, Integer> methodCountResult = new HashMap<>();

//        String fileAsString = FileUtils.readFileToString(new File(fileName));

        Scanner s = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(fileName)));
            StringBuilder nextString = new StringBuilder();
            while (s.hasNext())
            {
                String str = s.next();
                String currentString = str;
                int lastIndex = str.lastIndexOf(">>");
                if(lastIndex== -1 || lastIndex ==0) {
                    nextString.append(" ").append(currentString);
                } else {
                    try {
                        currentString = nextString + " " + str.substring(0, lastIndex);
                        analyzeResultString(result, methodCountResult, currentString);
                        nextString = new StringBuilder(str.substring(lastIndex));
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            if(!nextString.toString().isEmpty()) {
                analyzeResultString(result, methodCountResult, nextString.toString());
            }
        } catch (Error e )
        {
            System.out.println("Error");
        } finally {
            if (s != null) {
                s.close();
            }
        }

//        analyzeResultString(result, fileAsString);

        printResults(fileName, result, methodCountResult);

    }

    private void analyzeResultString(Map<String, Map<String, List<String>>> result, Map<String, Integer> methodCountResult, String fileAsString) {
        String[] lines = fileAsString.split(">>");
        for (String line : lines) {
            if (line.contains("...")) {
                String[] singleMethodExecutions = line.split("\\.\\.\\.");

                if(methodCountResult.containsKey(singleMethodExecutions[0])) {
                    methodCountResult.put(singleMethodExecutions[0], methodCountResult.get(singleMethodExecutions[0])+1);
                } else {
                    methodCountResult.put(singleMethodExecutions[0], 1);
                }

                    for(int i=1; i<singleMethodExecutions.length ; i++) {
                        String singleExecution = singleMethodExecutions[i];
                        String [] splitValues = singleExecution.split("-_\\{");

                        if(splitValues.length!=2) {
                            System.out.println(singleExecution);
                        }
                        Map<String , List<String>> singleMethodResults;
                        if(result.containsKey(splitValues[0])) {
                            singleMethodResults = result.get(splitValues[0]);
                        } else {
                            singleMethodResults = new HashMap<String, List<String>>();
                        }


                        String variableName = splitValues[1];
                        variableName = variableName.substring(0, variableName.indexOf("}"));

                        String variableValue = singleExecution.split("->\\{")[1];
                        variableValue = variableValue.substring(0, variableValue.indexOf("}"));

                        if(singleMethodResults.containsKey(variableName)) {
                            singleMethodResults.get(variableName).add(variableValue);
                        } else {
                            List<String> values = new ArrayList<String>();
                            values.add(variableValue);
                            singleMethodResults.put(variableName, values);
                        }
                        result.put(splitValues[0], singleMethodResults);
                    }

            } else {
                if(methodCountResult.containsKey(line)) {
                    methodCountResult.put(line, methodCountResult.get(line)+1);
                } else {
                    methodCountResult.put(line, 1);
                }
            }
        }
    }


    private void printResults(String fileName, Map<String, Map<String, List<String>>> result, Map<String, Integer> methodCountResult) throws IOException {
        File file = new File(fileName + "_summary_results.txt");
        Files.deleteIfExists(file.toPath());

        PrintWriter writer = new PrintWriter(fileName.replace("/raw/", "/summary/") + "_summary_results.txt", "UTF-8");
        writer.println("Method Location,No.of Calls of the Method,Variable Name,Unique Values,Total No.of Calls of the Variable,Values");

        System.out.println("RESULT>>> ");
        for (Map.Entry<String, Map<String, List<String>>> stringMapEntry : result.entrySet()) {
//            System.out.println("Method >>>>> " + stringMapEntry.getKey());
            for (Map.Entry<String, List<String>> entry : stringMapEntry.getValue().entrySet()) {
//                System.out.println("  Variable >> " + entry.getKey() + " : " + StringUtils.join(entry.getValue(), ","));

                Map<String, Integer> valueMap = new HashMap<>();
                for (String value : entry.getValue()) {
                    if(valueMap.containsKey(value)) {
                        valueMap.put(value, valueMap.get(value) + 1);
                    } else {
                        valueMap.put(value, 1);
                    }
                }
                StringBuilder builder = new StringBuilder();

                int total = 0;
                for (Map.Entry<String, Integer> valueEntry : valueMap.entrySet()) {
                    builder.append(",").append(valueEntry.getKey()).append(" x ").append(valueEntry.getValue());
                    total += valueEntry.getValue();
                }

                Integer methodCount = methodCountResult.get(stringMapEntry.getKey());
                if(methodCount==null) {
                    methodCount=0;
/*                    String[] methodDetails = stringMapEntry.getKey().split("->");
                    if(methodDetails.length!=2) {
                        System.out.println();
                    }
                    int start = Integer.parseInt(methodDetails[1].split(",")[0]);
                    int end = Integer.parseInt(methodDetails[1].split(",")[1]);

                    int newStart = Integer.MIN_VALUE;
                    int newEnd = Integer.MAX_VALUE;

                    for (Map.Entry<String, Integer> methodEntry : methodCountResult.entrySet()) {
                        String[] methodEntryDetails = methodEntry.getKey().split("->");
                        if(methodDetails[0].equals(methodEntryDetails[0])) {
                            int start_1 = Integer.parseInt(methodEntryDetails[1].split(",")[0]);
                            int end_1 = Integer.parseInt(methodEntryDetails[1].split(",")[1]);

                            if(start_1<=start && end_1 >= end && start_1 >= newStart && end_1 <= newEnd) {
                                newStart = start_1;
                                newEnd = end_1;
                                methodCount = methodEntry.getValue();
                            }
                        }
                    }

                    if(newStart==Integer.MIN_VALUE)
                        System.out.println("Error");
                    for (Map.Entry<String, Integer> methodEntry : methodCountResult.entrySet()) {
                        String[] methodEntryDetails = methodEntry.getKey().split("->");
                        if (methodDetails[0].equals(methodEntryDetails[0])) {
                            System.out.println(methodDetails[0]+"->"+methodDetails[1]+"->"+methodEntryDetails[1]);
                        }
                    }*/
                }
                writer.println(stringMapEntry.getKey().replace(",", "-") + "," + methodCount + "," + entry.getKey() + "," + valueMap.size() + "," + total + builder.toString());
            }
        }
        writer.close();
    }

}

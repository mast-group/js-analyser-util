import java.io.*;

/**
 * Created by Pankajan on 29/01/2016.
 */
public class VariableValuesIntoFiles {

    public static final String ROOT = "/Users/Pankajan/Edinburgh/Research_Source/Result/";

    /**
     * Save each variables into separate files with their values in each line
     * @param args
     * @throws IOException
     */
    /*public static void main(String[] args) throws IOException {
        File file = new File(ROOT);
        for (File resultSummary : file.listFiles()) {
            if(resultSummary.getName().contains("_summary_")) {
                BufferedReader reader = new BufferedReader(new FileReader(resultSummary));

                File resultFolder = new File(ROOT + resultSummary.getName().substring(4, resultSummary.getName().indexOf(".txt_summary_")));
                resultFolder.mkdir();

                String line;
                reader.readLine();
                while((line = reader.readLine()) != null) {
                    String[] lineContent = line.split(",");
                    PrintWriter writer = new PrintWriter(resultFolder.getAbsolutePath() + "/" + lineContent[0].substring(lineContent[0].lastIndexOf("/")) + "_" + lineContent[2] + ".csv" , "UTF-8");

                    for(int i=5; i<lineContent.length; i++) {
                        String[] values = lineContent[i].split(" x ");
                        for(int j=0; j < Integer.parseInt(values[1]); j++) {
                            writer.println(values[0]);
                        }
                    }
                    writer.flush();
                    writer.close();

                }

            }
        }
    }*/

    /**
     * Save variables from one project in single file with variable name in one column and value in another
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File file = new File(ROOT);
        /*for (File resultSummary : file.listFiles()) {
            if(resultSummary.getName().contains("_summary_")) {*/

        File resultSummary = new File(ROOT + "log_Ghost.txt_summary_results.txt");
                BufferedReader reader = new BufferedReader(new FileReader(resultSummary));

                File resultFile = new File(ROOT + resultSummary.getName().substring(4, resultSummary.getName().indexOf(".txt_summary_")) + "_variable_summary.csv");
                PrintWriter writer = new PrintWriter(resultFile, "UTF-8");

                String line;
                reader.readLine();
                while((line = reader.readLine()) != null) {
                    String[] lineContent = line.split(",");
                    String variableName = lineContent[0].substring(lineContent[0].lastIndexOf("/")+1) + "_" + lineContent[2];
                    for(int i=5; i<lineContent.length; i++) {
                        String[] values = lineContent[i].split(" x ");
                        for (int j = 0; j < Integer.parseInt(values[1]); j++) {
                            writer.println(variableName + "," + values[0]);
                        }
                    }
                }
                writer.flush();
                writer.close();
            /*}
        }*/
    }
}

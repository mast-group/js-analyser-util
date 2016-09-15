package variable.summary;

import common.utils.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pankajan on 06/02/2016.
 */
public class VariableQuartiles {
    /**
     * Save variables from one project/many projects in single file with variable name in one column and value in another
     * @param path
     * @throws IOException
     */
    public void saveVariableQuartilesInSingleFile(String path, String output, int thresholdUniqueValues, int thresholdTotalValues) throws IOException {
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        File resultFile = new File(output);
        PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
        writer.println("Name,min,1st quartile,median,3rd quartile,max,norm-min,norm-1st quartile,norm-median,norm-3rd quartile,norm-max");

        String line;
        reader.readLine();
        while((line = reader.readLine()) != null) {
            String[] lineContent = line.split(",");
            String variableName = lineContent[0].substring(lineContent[0].lastIndexOf("/")+1) + "_" + lineContent[2];
            if((Integer.parseInt(lineContent[3]) == -1 || Integer.parseInt(lineContent[3]) >= thresholdUniqueValues) &&
                    (Integer.parseInt(lineContent[4]) == -1 || Integer.parseInt(lineContent[4]) >= thresholdTotalValues)) {
                List<Double> variableValues = new ArrayList<>();
                for (int i = 5; i < lineContent.length; i++) {
                    String[] values = lineContent[i].split(" x ");
                    for (int j = 0; j < Integer.parseInt(values[1]); j++) {
                        variableValues.add(Double.parseDouble(values[0]));
                    }
                }

                double[] primitiveArray = getPrimitiveArray(variableValues);
                DescriptiveStatistics descriptiveStatistics = getDescriptiveStatistics(primitiveArray);

                double[] normalizedVariableValues = getNormalizedValues(primitiveArray, descriptiveStatistics.getMin(), descriptiveStatistics.getMax());
                DescriptiveStatistics normalizedDescriptiveStatistics = getDescriptiveStatistics(normalizedVariableValues);

                writer.println(variableName + ","
                        + descriptiveStatistics.getMin() + "," + descriptiveStatistics.getPercentile(25) + "," + descriptiveStatistics.getPercentile(50) + "," + descriptiveStatistics.getPercentile(75)+ "," + descriptiveStatistics.getMax() + "," +
                        normalizedDescriptiveStatistics.getMin() + "," + normalizedDescriptiveStatistics.getPercentile(25) + "," + normalizedDescriptiveStatistics.getPercentile(50) + "," + normalizedDescriptiveStatistics.getPercentile(75)+ "," + normalizedDescriptiveStatistics.getMax());
            }
        }
        writer.flush();
        writer.close();
    }


    private double[] getPrimitiveArray(List<Double> variableValues) {
        Double [] values = new Double[variableValues.size()];
        variableValues.toArray(values);
        return ArrayUtils.toPrimitive(values);
    }

    private DescriptiveStatistics getDescriptiveStatistics(double[] variableValues) {
        return new DescriptiveStatistics(variableValues);
    }

    private double[] getNormalizedValues(double[] variableValues, double min, double max) {
        double [] normalizedValues = new double[variableValues.length];
        double minMaxDiff = max - min;
        for (int i = 0; i < variableValues.length; i++) {
            double primitiveValue = variableValues[i];
            normalizedValues[i] = (primitiveValue - min)/minMaxDiff;
        }

        return normalizedValues;
    }
}

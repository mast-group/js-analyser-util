import org.apache.commons.io.FileUtils;
import sun.text.normalizer.UTF16;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pankajan on 10/01/2016.
 */
public class CountMethodCalls {

    public static void main(String[] args) throws IOException {
        String fileName = "/Users/Pankajan/log_d3.txt";
        CountMethodCalls countMethodCalls = new CountMethodCalls();
        countMethodCalls.count(fileName);
    }

    private void count(String fileName) throws IOException {
        String fileAsString = FileUtils.readFileToString(new File(fileName));
        String[] lines = fileAsString.split("\\.\\.\\.");
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String line : lines) {
           if(map.containsKey(line)) {
               map.put(line, map.get(line) + 1);
           } else {
               map.put(line, 1);
           }
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if(entry.getValue() > 100)
                System.out.println(entry.getKey() + " -> " + entry.getValue() );
        }


    }
}

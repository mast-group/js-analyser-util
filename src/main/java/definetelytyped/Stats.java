package definetelytyped;

import java.io.File;

/**
 * Created by Pankajan on 17/06/2016.
 */
public class Stats {
    private static final String ROOT_FOLDER = "";

    public static void main(String[] args) {
        collectData(new File(ROOT_FOLDER));
    }

    private static void collectData(File rootFolder) {
        for(File eachFolder : rootFolder.listFiles()) {
            if(eachFolder.isDirectory()) {
                for(File file : eachFolder.listFiles()) {
                    if (file.getName().contains("tests")){

                    } else {

                    }
                }
            }
        }
    }

}

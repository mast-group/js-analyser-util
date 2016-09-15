package github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.utils.MapUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Pankajan on 12/07/2016.
 */
public class ListChangedFiles {
    private static final String OWNER = "DefinitelyTyped";
    private static final String REPO = "DefinitelyTyped";

    private static Map<String, Integer> fileCount = new HashMap<>();
    private static Map<String, String> commitList = new HashMap<>();

    public static void main(String[] args) {
        String commit_id_list_file = "/Users/Pankajan/Edinburgh/Results/DefinitelyTyped_commit_url_signature_changes.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(commit_id_list_file)));
            String url;
            while((url = reader.readLine()) != null) {
                getCommitDetails(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Integer> sortedFileCount = MapUtil.sortByValue(fileCount);
        for (Map.Entry<String, Integer> entry : sortedFileCount.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
            System.out.println(commitList.get(entry.getKey()));
        }
    }

    public static void getCommitDetails (String url) throws IOException {
        String commit_sha = url.split("/")[6];
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(GithubConstants.BASE_REPOS_URL + OWNER + "/" + REPO + "/commits/" + commit_sha + "?access_token=" + GithubConstants.GITHUB_ACCESS_TOKEN);
        httpClient.executeMethod(method);
        JsonElement result = new JsonParser().parse(method.getResponseBodyAsString());
        method.releaseConnection();

        try {
            for (JsonElement jsonElement : result.getAsJsonObject().get("files").getAsJsonArray()) {
                String filename = jsonElement.getAsJsonObject().get("filename").getAsString();
                if (fileCount.containsKey(filename)) {
                    fileCount.put(filename, fileCount.get(filename) + 1);
                    commitList.put(filename, commitList.get(filename) + "\n" + url);
                } else {
                    fileCount.put(filename, 1);
                    commitList.put(filename, url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

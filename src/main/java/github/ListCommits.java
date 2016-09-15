package github;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;

/**
 * Created by Pankajan on 12/07/2016.
 */
public class ListCommits {
    private static final String OWNER = "DefinitelyTyped";
    private static final String REPO = "DefinitelyTyped";

    public static void main(String[] args) {
        String commit_id_list_file = "/Users/Pankajan/Edinburgh/Results/DefinitelyTyped_bug_or_fix_commit_ids.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(commit_id_list_file)));
            String commit_id_with_msg;
            while((commit_id_with_msg = reader.readLine()) != null) {
                getCommitDetails(commit_id_with_msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void getCommitDetails (String commit_id_with_msg) throws IOException {
        String commitId = commit_id_with_msg.split(" ")[0];
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(GithubConstants.BASE_REPOS_URL + OWNER + "/" + REPO + "/commits/" + commitId + "?access_token=" + GithubConstants.GITHUB_ACCESS_TOKEN);
        httpClient.executeMethod(method);
        JsonElement result = new JsonParser().parse(method.getResponseBodyAsString());
        method.releaseConnection();

        try {
            JsonObject stats = result.getAsJsonObject().get("stats").getAsJsonObject();
            if (stats.get("additions").getAsInt() < 5 && stats.get("deletions").getAsInt() <5) {
                System.out.println(commit_id_with_msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

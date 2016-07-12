package github;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Pankajan on 07/04/2016.
 */
public class ListRepositories {
    private static final String GITHUB_ACCESS_TOKEN = "8d3023edc3f9fca1288417d4cd071d0df3803282";
    private static final String BASE_URL = "https://api.github.com/search/repositories?";

    private static final int PAGE_COUNT = 100;
    private static final int PAGE_SIZE = 100;
    private static final String EXTRA_PARAMS = "q=+language:javascript&sort=stars&order=desc";
    private static final String OUTPUT_FILE = "/Users/Pankajan/Edinburgh/Results/js_projects_list";

    public void execute() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(OUTPUT_FILE)));
        for(int i=1; i<= PAGE_COUNT; i++) {
            JsonElement resultPerPage = getResultPerPage(i);
            for (JsonElement item : resultPerPage.getAsJsonObject().get("items").getAsJsonArray()) {
                writer.append(item.getAsJsonObject().get("html_url").getAsString());
                writer.newLine();
            }
            writer.flush();
        }
        writer.close();
    }

    public static void main(String[] args) {
        ListRepositories listRepositories = new ListRepositories();
        try {
            listRepositories.execute();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public JsonElement getResultPerPage (int page) throws IOException {
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(BASE_URL + EXTRA_PARAMS + "&page=" + page + "&per_page=" + PAGE_SIZE );
        httpClient.executeMethod(method);
        JsonElement result = new JsonParser().parse(method.getResponseBodyAsString());
        method.releaseConnection();
        return result;
    }
}

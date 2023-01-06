package se.mwthinker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static java.net.HttpURLConnection.HTTP_OK;

public class Github {
    public Github() {
    }

    public String fetchLatestCommitSHA(String owner, String repo) {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/commits?per_page=1&page=1")
                .addHeader("Accept", "application/json")
                .build();

        var client = new OkHttpClient().newBuilder().build();

        try (Response response = client.newCall(request).execute()) {
            if (HTTP_OK != response.code()) {
                throw new RuntimeException(response.message());
            }
            return Arrays.stream(new ObjectMapper().readValue(Objects.requireNonNull(response.body()).string(), RepoCommitContent[].class))
                    .findFirst()
                    .map(RepoCommitContent::getSha)
                    .orElseThrow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VcpkgObject fetchVcpkgObject(String owner, String repo, String commitSha) {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/contents/vcpkg.json" + "?" + commitSha)
                .method("GET", null)
                .addHeader("Accept", "application/vnd.github.object")
                .build();

        var client = new OkHttpClient().newBuilder().build();

        try (Response response = client.newCall(request).execute()) {
            if (HTTP_OK != response.code()) {
                throw new RuntimeException(response.message());
            }
            return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).string(), RepoFileContent.class).getVcpkgObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        var github = new Github();
        String sha = github.fetchLatestCommitSHA("mwthinker", "CppSdl2");
        VcpkgObject ob = github.fetchVcpkgObject("mwthinker", "CppSdl2", sha);
        System.out.println(sha);
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(ob));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

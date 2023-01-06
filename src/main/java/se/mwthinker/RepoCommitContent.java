package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoCommitContent {
    @JsonProperty("sha")
    private String sha;

    public String getSha() {
        return sha;
    }

}

package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoCommitContent {
    @JsonProperty("sha")
    private String sha;

    public String getSha() {
        return sha;
    }


    /*
    public Optional<String> getFirstCommitSha() {
        return commits.stream()
                .findFirst()
                .map(commit -> Optional.ofNullable(commit.sha))
                .flatMap(Function.identity());
    }
    */
}

/*
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoCommitContent {
    @JsonProperty("sha")
    private final List<Commit> commits = new ArrayList<>();

    private static class Commit {
        @JsonProperty("sha")
        String sha;
    }

    public Optional<String> getFirstCommitSha() {
        return commits.stream()
                .findFirst()
                .map(commit -> Optional.ofNullable(commit.sha))
                .flatMap(Function.identity());
    }
}

 */
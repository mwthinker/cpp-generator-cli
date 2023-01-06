package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoFileContent {
    @JsonProperty("name")
    private String name = "";

    @JsonProperty("content")
    @JsonDeserialize(using = Base64Deserializer.class)
    private VcpkgObject vcpkgObject;

    public String getName() {
        return name;
    }

    public VcpkgObject getVcpkgObject() {
        return vcpkgObject;
    }
}

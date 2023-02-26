package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "name", "version-string", "port-version", "homepage", "description", "license", "dependencies" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class VcpkgObject {

    @JsonProperty("name")
    private String name = "";

    @JsonProperty("version-string")
    private String version = "0.1.0";

    @JsonProperty("homepage")
    private String homepage = "";

    @JsonProperty("description")
    private String description = "";

    @JsonProperty("license")
    private String license = "MIT";

    @JsonProperty("dependencies")
    private final List<String> dependencies = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public void addDependencies(List<String> dependencies) {
        this.dependencies.addAll(dependencies);
    }

}

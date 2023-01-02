package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "name", "version-string", "port-version", "homepage", "description", "license", "dependencies" })
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
    private List<String> dependencies = new ArrayList<>();

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

    public void saveToFile(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(
                    new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed("\n"))
            );
            writer.writeValue(file, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

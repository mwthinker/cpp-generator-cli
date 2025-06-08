package se.mwthinker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "default-registry", "registries" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class VcpkgConfigurationObject {

    @JsonProperty("default-registry")
    private DefaultRegistry defaultRegistry;

    @JsonProperty("registries")
    private List<Registry> registries = new ArrayList<>();

    public DefaultRegistry getDefaultRegistry() {
        return defaultRegistry;
    }

    public void setDefaultRegistry(DefaultRegistry defaultRegistry) {
        this.defaultRegistry = defaultRegistry;
    }

    public List<Registry> getRegistries() {
        return registries;
    }

    public void setRegistries(List<Registry> registries) {
        this.registries = registries;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DefaultRegistry {
        @JsonProperty("kind")
        private String kind;

        @JsonProperty("baseline")
        private String baseline;

        @JsonProperty("repository")
        private String repository;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getBaseline() {
            return baseline;
        }

        public void setBaseline(String baseline) {
            this.baseline = baseline;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Registry {
        @JsonProperty("kind")
        private String kind;

        @JsonProperty("baseline")
        private String baseline;

        @JsonProperty("repository")
        private String repository;

        @JsonProperty("packages")
        private List<String> packages = new ArrayList<>();

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getBaseline() {
            return baseline;
        }

        public void setBaseline(String baseline) {
            this.baseline = baseline;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository;
        }

        public List<String> getPackages() {
            return packages;
        }

        public void setPackages(List<String> packages) {
            this.packages = packages;
        }
    }
}

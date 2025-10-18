package se.mwthinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

enum LicenseType {
    MIT
}

public class CMakeBuilder {

    public record ExternalProject(String name, String gitUrl, String gitTag) {}

    private boolean testProject;
    private final FileSystem fileSystem;
    private final Github github;
    private final List<ExternalProject> externalProjects = new ArrayList<>();
    private final List<VcpkgObject> vcpkgObjects = new ArrayList<>();
    private final Set<String> vcpkgDependencies = new LinkedHashSet<>(); // Want to element keep order (to make it easier for a human to read).
    private final Set<String> fetchedVcpkgDependencies = new LinkedHashSet<>();
    private final Set<String> linkLibraries = new LinkedHashSet<>();
    private final Set<String> sources = new LinkedHashSet<>();
    private final Set<String> extraFiles = new LinkedHashSet<>();
    private String description = "Description";
    private String author = "";
    private final List<VcpkgConfigurationObject.Registry> registries = new ArrayList<>();

    public CMakeBuilder(FileSystem fileSystem, Github github) {
        this.fileSystem = fileSystem;
        this.github = github;
    }

    public CMakeBuilder addExternalProjects(String name, String gitUrl, String gitTag) {
        externalProjects.add(new ExternalProject(name, gitUrl, gitTag));
        return this;
    }

    public CMakeBuilder addRegistry(String owner, String repo, String... packages) {
        String commitSha = github.fetchLatestCommitSHA(owner, repo);
        var registry = new VcpkgConfigurationObject.Registry();
        registry.setKind("git");
        registry.setBaseline(commitSha);
        registry.setRepository(Github.getRepositoryUrl(owner, repo));
        if (packages != null && packages.length > 0) {
            registry.setPackages(List.of(packages));
        }
        registries.add(registry);
        return this;
    }

    public CMakeBuilder addExternalProjectsWithDependencies(String owner, String repo) {
        var repositoryUrl = Github.getRepositoryUrl(owner, repo);
        String commitSha = github.fetchLatestCommitSHA(owner, repo);
        var vcpkgObject = github.fetchVcpkgObject(owner, repo, commitSha);
        vcpkgObjects.add(vcpkgObject);
        fetchedVcpkgDependencies.addAll(vcpkgObject.getDependencies());
        return addExternalProjects(repo, repositoryUrl, commitSha);
    }

    public CMakeBuilder addVcpkgDependency(String dependency) {
        vcpkgDependencies.add(dependency);
        return this;
    }

    public CMakeBuilder addLinkLibrary(String library) {
        linkLibraries.add(library);
        return this;
    }

    public CMakeBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CMakeBuilder addSource(String source) {
        sources.add(source);
        return this;
    }

    public CMakeBuilder addExtraFile(String file) {
        extraFiles.add(file);
        return this;
    }

    public CMakeBuilder withTestProject(boolean addTestProject) {
        this.testProject = addTestProject;
        return this;
    }

    public CMakeBuilder withLicense(LicenseType type, String author) {
        this.author = author;
        return this;
    }

    public void buildFiles() {
        if (sources.isEmpty()) {
            throw new RuntimeException("Must at least have one source file");
        }

        for (var source : sources) {
            fileSystem.copyResourceTo(source);
        }

        addExtraFile("CMakePresets.json");
        fileSystem.copyResourceTo("CMakePresets.json");
        addExtraFile("vcpkg.json");
        saveVcpkgJson();
        saveVcpkgConfigurationJson();

        fileSystem.copyResourceTo("gitattributes",".gitattributes");
        fileSystem.copyResourceTo("gitignore", ".gitignore");

        if (!externalProjects.isEmpty()) {
            fileSystem.saveFileFromTemplate(Map.of("externalProjects", externalProjects), "ExternalFetchContent.cmake");
            addExtraFile("ExternalFetchContent.cmake");
        }

        saveCMakeListsTxt();
        if (!author.isEmpty()) {
            saveLicenseFile();
        }

        saveGithubAction();
        throw new RuntimeException("testar detta");
    }

    private void saveLicenseFile() {
        fileSystem.saveFileFromTemplate(Map.of("author", author), "LICENSE");
    }

    private void saveGithubAction() {
        Map<String, Object> data = new HashMap<>();
        data.put("projectName", fileSystem.getProjectName());
        data.put("hasTests", testProject);

        fileSystem.saveFileFromTemplate(data, ".github/workflows/ci.yml");
    }

    private void saveVcpkgConfigurationJson() {
        var newVcpkgConfiguration = new VcpkgConfigurationObject();
        var defaultRegistry = new VcpkgConfigurationObject.DefaultRegistry();
        newVcpkgConfiguration.setDefaultRegistry(defaultRegistry);
        defaultRegistry.setKind("git");
        var commitSHA = github.fetchLatestCommitSHA("microsoft", "vcpkg");
        defaultRegistry.setBaseline(commitSHA);
        defaultRegistry.setRepository(Github.getRepositoryUrl("microsoft", "vcpkg"));

        if (!registries.isEmpty()) {
            newVcpkgConfiguration.setRegistries(registries);
        }

        fileSystem.saveToFile(newVcpkgConfiguration, "vcpkg-configuration.json");
    }

    private void saveVcpkgJson() {
        var newVcpkgObject = new VcpkgObject();
        newVcpkgObject.setName(fileSystem.getProjectName().toLowerCase());
        newVcpkgObject.setDescription(description);

        vcpkgDependencies.forEach(newVcpkgObject::addDependency);
        fetchedVcpkgDependencies.forEach(newVcpkgObject::addDependency);

        if (testProject) {
            newVcpkgObject.addDependency("gtest");
        }
        fileSystem.saveToFile(newVcpkgObject, "vcpkg.json");

        if (testProject) {
            buildTestProject();
        }
    }

    private String getTestProjectName() {
        return fileSystem.getProjectName() + "_Test";
    }

    private void buildTestProject() {
        fileSystem.copyResourceTo(pathOf(getTestProjectName(), "src", "tests.cpp"));

        Map<String, Object> data = new HashMap<>();
        data.put("projectName", getTestProjectName());
        data.put("extraFiles", List.of("CMakeLists.txt"));

        fileSystem.saveFileFromTemplate(data, "Test_CMakeLists.ftl", pathOf(getTestProjectName(), "CMakeLists.txt"));
    }

    private void saveCMakeListsTxt() {
        Map<String, Object> data = new HashMap<>();
        data.put("projectName", fileSystem.getProjectName());
        data.put("description", description);
        data.put("sources", sources);
        data.put("vcpkgDependencies", vcpkgDependencies);
        data.put("linkLibraries", linkLibraries);
        if (testProject) {
            data.put("testProjectName", getTestProjectName());
        }
        if (!externalProjects.isEmpty()) {
            data.put("linkExternalLibraries", externalProjects);
        }
        data.put("extraFiles", extraFiles);

        fileSystem.saveFileFromTemplate(data, "CMakeLists.txt");
    }

    private static String pathOf(String... paths) {
        if (paths.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(paths)
                .limit(paths.length - 1)
                .forEach(path -> builder.append(path).append("/"));
        return builder.append(paths[paths.length - 1]).toString();
    }

}

package se.mwthinker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private final VcpkgObjectFactory vcpkgObjectFactory;
    private final List<ExternalProject> externalProjects = new ArrayList<>();
    private final List<VcpkgObject> vcpkgObjects = new ArrayList<>();
    private final Set<String> vcpkgDependencies = new LinkedHashSet<>(); // Want to element keep order (to make it easier for a human to read).
    private final Set<String> linkLibraries = new LinkedHashSet<>();
    private final Set<String> sources = new LinkedHashSet<>();
    private final Set<String> extraFiles = new LinkedHashSet<>();
    private String description = "Description";
    private String author = "";

    public CMakeBuilder(FileSystem fileSystem, Github github, VcpkgObjectFactory vcpkgObjectFactory) {
        this.vcpkgObjectFactory = vcpkgObjectFactory;
        this.fileSystem = fileSystem;
        this.github = github;
    }

    public CMakeBuilder addExternalProjects(String name, String gitUrl, String gitTag) {
        externalProjects.add(new ExternalProject(name, gitUrl, gitTag));
        return this;
    }

    public CMakeBuilder addExternalProjectsWithDependencies(String owner, String repo) {
        var repositoryUrl = github.getRepositoryUrl(owner, repo);
        String commitSha = github.fetchLatestCommitSHA(owner, repo);
        var vcpkgObject = github.fetchVcpkgObject(owner, repo, commitSha);
        vcpkgObjects.add(vcpkgObject);
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
            File file = fileSystem.createFile(source);
            fileSystem.copyResourceTo(file.getName(), file.getParentFile());
        }

        addExtraFile("CMakePresets.json");
        fileSystem.copyResourceTo("CMakePresets.json");
        addExtraFile("vcpkg.json");
        saveVcpkgJson();

        fileSystem.copyResourceTo("gitattributes",".gitattributes");
        fileSystem.copyResourceTo("gitignore", ".gitignore");

        if (!externalProjects.isEmpty()) {
            fileSystem.saveFileFromTemplate(Map.of("externalProjects", externalProjects),
                    "ExternalFetchContent.ftl",
                    fileSystem.createFile("ExternalFetchContent.cmake"));
            addExtraFile("ExternalFetchContent.cmake");
        }

        saveCMakeListsTxt();
        if (!author.isEmpty()) {
            saveLicenseFile();
        }

        saveGithubAction();
    }

    private void saveLicenseFile() {
        fileSystem.saveFileFromTemplate(Map.of("author", author), "LICENSE.ftl", fileSystem.createFile( "LICENSE"));
    }

    private void saveGithubAction() {
        Map<String, Object> data = new HashMap<>();
        data.put("projectName", fileSystem.getProjectName());
        data.put("hasTests", testProject);

        File workflowsDir = fileSystem.createFolder(".github/workflows");

        fileSystem.saveFileFromTemplate(data, "ci.ftl", fileSystem.createFile(workflowsDir, "ci.yml"));
    }

    private void saveVcpkgJson() {
        var newVcpkgObject = vcpkgObjectFactory.createVcpkgObject(fileSystem.getProjectName().toLowerCase(), description);
        vcpkgDependencies.forEach(newVcpkgObject::addDependency);

        Set<String> dependencies = new HashSet<>(vcpkgDependencies);
        dependencies.addAll(vcpkgObjects.stream()
                .flatMap(vcpkgObject -> vcpkgObject.getDependencies().stream())
                .toList());
        newVcpkgObject.addDependencies(dependencies.stream().toList());

        if (testProject) {
            newVcpkgObject.addDependency("gtest");
        }
        newVcpkgObject.saveToFile(fileSystem.createFile("vcpkg.json"));

        if (testProject) {
            buildTestProject();
        }
    }

    private String getTestProjectName() {
        return fileSystem.getProjectName() + "_Test";
    }

    private void buildTestProject() {
        File testProjectDir = fileSystem.createFolder(getTestProjectName());
        File sourceDir = fileSystem.createFolder(testProjectDir, "src");
        fileSystem.copyResourceTo("tests.cpp", sourceDir);

        Map<String, Object> data = new HashMap<>();
        data.put("projectName", getTestProjectName());
        data.put("extraFiles", List.of("CMakeLists.txt"));

        fileSystem.saveFileFromTemplate(data, "Test_CMakeLists.ftl", fileSystem.createFile(testProjectDir, "CMakeLists.txt"));
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

        fileSystem.saveFileFromTemplate(data, "CMakeLists.ftl", fileSystem.createFile("CMakeLists.txt"));
    }

}

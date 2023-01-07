package se.mwthinker;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CMakeBuilder {
    private record ExternalProject(String name, String gitUrl, String gitTag) {
        @Override
        public String toString() {
            return "FetchContent_Declare(" + name + "\n" +
                    "\tGIT_REPOSITORY\n" +
                    "\t\t" + gitUrl + "\n" +
                    "\tGIT_TAG\n" +
                    "\t\t"+ gitTag + "\n" +
                    "\tOVERRIDE_FIND_PACKAGE\n" +
                    ")";
        }
    }

    private final File projectDir;
    private final ResourceHandler resourceHandler;
    private final List<ExternalProject> externalProjects = new ArrayList<>();
    private final Set<String> vcpkgDependencies = new LinkedHashSet<>(); // Want to element keep order (to make it easier for a human to read).
    private final Set<String> sources = new LinkedHashSet<>();
    private String description = "Description";

    public CMakeBuilder(File projectDir, ResourceHandler resourceHandler) {
        this.projectDir = projectDir;
        this.resourceHandler = resourceHandler;
    }

    public CMakeBuilder addExternalProjects(String name, String gitUrl, String gitTag) {
        externalProjects.add(new ExternalProject(name, gitUrl, gitTag));
        return this;
    }

    public CMakeBuilder addVcpkgDependency(String dependency) {
        vcpkgDependencies.add(dependency);
        return this;
    }

    public CMakeBuilder addVcpkgDependencies(List<String> dependencies) {
        vcpkgDependencies.addAll(dependencies);
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

    public void buildFiles() {
        if (sources.isEmpty()) {
            throw new RuntimeException("Must at least have one source file");
        }

        saveCMakeListsTxt();

        if (!externalProjects.isEmpty()) {
            StringBuilder content = new StringBuilder("include(FetchContent)\n\n");
            for (var item : externalProjects) {
                content.append(item);
            }
            Util.saveToFile(new File(projectDir, "ExternalFetchContent.cmake"), content.toString());
        }

        saveVcpkgJson();

        resourceHandler.copyResourceTo("CMakePresets.json", projectDir);
    }

    private void saveVcpkgJson() {
        var vcpkgObject = new VcpkgObject();
        vcpkgObject.setName(projectDir.getName().toLowerCase());
        vcpkgObject.setDescription(description);
        vcpkgDependencies.forEach(vcpkgObject::addDependency);
        vcpkgObject.saveToFile(new File(projectDir, "vcpkg.json"));
    }

    private void saveCMakeListsTxt() {
        String text = resourceHandler.resourceAsString("CMakeLists.txt")
                .replace("NewProject", projectDir.getName())
                .replace("NewDescription", description);

        StringBuilder sourcesBuilder = new StringBuilder();
        for (var source : sources) {
            sourcesBuilder.append("\t").append(source).append("\n");
        }
        text = text.replace("Sources", sourcesBuilder.toString());

        if (externalProjects.isEmpty()) {
            text = text.replace("ExternalProjects", "");
            text = text.replace("ExtraFiles", """
                    \tCMakePresets.json
                    \tvcpkg.json
                    """);
        } else {
            text = text.replace("ExtraFiles", """
                    
                    \tCMakePresets.json
                    \tvcpkg.json
                    """);

            String findPackages = """
                    set(ExternalDependencies 
                    \tLinkExternalLibraries)
                    
                    include(ExternalFetchContent.cmake)
                    foreach(Dependency IN LISTS ExternalDependencies)
                    \tfind_package(${Dependency} REQUIRED)
                    endforeach()""";
            text = text.replace("ExternalProjects", findPackages);

            StringBuilder linkExternalLibraries = new StringBuilder();
            for (String name : externalProjects.stream().map(ExternalProject::name).toList()) {
                linkExternalLibraries.append(name).append("\n");
            }
            text = text.replace("LinkExternalLibraries", linkExternalLibraries.toString());
        }

        Util.saveToFile(new File(projectDir, "CMakeLists.txt"), text);
    }

}

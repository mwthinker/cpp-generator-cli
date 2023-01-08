package se.mwthinker;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "cppgen", mixinStandardHelpOptions = true, description = "C++ generator")
public class GeneratorCli implements Callable<Integer> {
    @Option(names = { "-n", "--new" }, required = true, paramLabel = "NEW", description = "the project name")
    private File projectDir;

    @Option(names = { "-d", "--description" }, paramLabel = "DESCRIPTION ", description = "short description used in the template")
    private String description = "Description";

    @Option(names = { "-V", "--verbose" }, paramLabel = "VERBOSE", description = "show verbose output")
    private boolean verbose = false;

    @Option(names = { "-c", "--cmake" }, paramLabel = "OPEN", description = "run cmake")
    private boolean cmake = false;

    @Option(names = { "-o", "--open" }, paramLabel = "OPEN", description = "open visual studio solution")
    private boolean open = false;

    @Option(names = { "-g", "--gui" }, paramLabel = "GUI", description = "add gui library")
    private boolean gui = false;

    @Option(names = { "-t", "--test" }, paramLabel = "GUI", description = "add test")
    private boolean test = false;

    public GeneratorCli() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GeneratorCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (projectDir.exists() || !projectDir.mkdir()) {
            System.out.println("Failed to create project folder");
            return 1;
        }

        var srcDir = Util.createFolder(projectDir, "src");
        Util.createFolder(projectDir, "data");

        ResourceHandler resourceHandler;
        if (gui) {
            resourceHandler = new ResourceHandler("gui-template");
        } else {
            resourceHandler = new ResourceHandler("empty-template");
        }

        CMakeBuilder cmakeBuilder = new CMakeBuilder(projectDir, resourceHandler)
                .withDescription(description)
                .withTestProject(test);

        if (gui) {
            Github github = new Github();
            var repositoryUrl = github.getRepositoryUrl("mwthinker", "CppSdl2");
            String commitSha = github.fetchLatestCommitSHA("mwthinker", "CppSdl2");
            var vcpkgObject = github.fetchVcpkgObject("mwthinker", "CppSdl2", commitSha);

            cmakeBuilder
                    .addExternalProjects(
                            "CppSdl2",
                            repositoryUrl,
                            commitSha)
                    .addSource("src/main.cpp")
                    .addSource("src/testwindow.cpp")
                    .addSource("src/testwindow.h")
                    .addVcpkgDependencies(vcpkgObject.getDependencies());

            resourceHandler.copyResourceTo("main.cpp", srcDir);
            resourceHandler.copyResourceTo("testwindow.cpp", srcDir);
            resourceHandler.copyResourceTo("testwindow.h", srcDir);
        } else {
            cmakeBuilder
                    .addSource("src/main.cpp")
                    .addVcpkgDependency("fmt");
            resourceHandler.copyResourceTo("main.cpp", srcDir);
        }

        cmakeBuilder.buildFiles();

        if (cmake || open) {
            File buildDir = Util.createFolder(projectDir, "build");
            CMake.setVerbose(verbose);
            CMake.generate(projectDir, buildDir);
            if (open) {
                CMake.openVisualStudio(projectDir, buildDir);
            }
        }

        return 0;
    }

}

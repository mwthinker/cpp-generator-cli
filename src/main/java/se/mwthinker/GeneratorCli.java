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

    @Option(names = { "-t", "--test" }, paramLabel = "TEST", description = "add test")
    private boolean test = false;

    @Option(names = { "-l", "--license" }, paramLabel = "LICENSE", description = "add MIT license with author")
    private String author = "";

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
                .withTestProject(test)
                .addExtraFile("CMakePresets.json")
                .addExtraFile("vcpkg.json")
                .withLicense(LicenseType.MIT, author);

        if (gui) {
            cmakeBuilder
                    .addExternalProjectsWithDependencies("mwthinker","CppSdl2")
                    .addSource("src/main.cpp")
                    .addSource("src/testwindow.cpp")
                    .addSource("src/testwindow.h")
                    .addExtraFile("ExternalFetchContent.cmake");

            resourceHandler.copyResourceTo("main.cpp", srcDir);
            resourceHandler.copyResourceTo("testwindow.cpp", srcDir);
            resourceHandler.copyResourceTo("testwindow.h", srcDir);
        } else {
            cmakeBuilder
                    .addSource("src/main.cpp")
                    .addVcpkgDependency("fmt")
                    .addLinkLibrary("fmt::fmt");

            resourceHandler.copyResourceTo("main.cpp", srcDir);
        }
        resourceHandler.copyResourceTo(".gitattributes", projectDir);
        resourceHandler.copyResourceTo(".gitignore", projectDir);

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

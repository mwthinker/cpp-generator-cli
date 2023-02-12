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

    private ResourceHandler createResourceHandler() {
        if (gui) {
            return new ResourceHandler("gui-template");
        }

        return new ResourceHandler("empty-template");
    }

    @Override
    public Integer call() {
        if (projectDir.exists() || !projectDir.mkdir()) {
            System.out.println("Failed to create project folder");
            return 1;
        }
        if (!new File(projectDir, "data").mkdir()) {
            System.out.println("Failed to create data folder");
            return 1;
        }

        FileSystem fileSystem = new FileSystem(projectDir, createResourceHandler());

        CMakeBuilder cmakeBuilder = new CMakeBuilder(fileSystem, new Github(), new VcpkgObjectFactory())
                .withDescription(description)
                .withTestProject(test)
                .withLicense(LicenseType.MIT, author);

        if (gui) {
            cmakeBuilder
                    .addExternalProjectsWithDependencies("mwthinker","CppSdl2")
                    .addSource("src/main.cpp")
                    .addSource("src/testwindow.cpp")
                    .addSource("src/testwindow.h");
        } else {
            cmakeBuilder
                    .addSource("src/main.cpp")
                    .addVcpkgDependency("fmt")
                    .addLinkLibrary("fmt::fmt");
        }
        cmakeBuilder.buildFiles();

        if (cmake || open) {
            File buildDir = fileSystem.createFolder(projectDir, "build");
            CMake.setVerbose(verbose);
            CMake.generate(projectDir, buildDir);
            if (open) {
                CMake.openVisualStudio(projectDir, buildDir);
            }
        }

        return 0;
    }

}

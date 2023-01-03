package se.mwthinker;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "cppgen", mixinStandardHelpOptions = true, description = "C++ generator")
public class GeneratorCli implements Callable<Integer> {
    @Option(names = { "-n", "--new" }, required = true, paramLabel = "NEW", description = "the project name")
    private File projectDir;

    @Option(names = { "-v", "--vcpkg" }, paramLabel = "VCPKG_ROOT", description = "the directory containing the vcpkg repository (overrides the env variable)")
    private Path vcpkgPath;

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

    private final ResourceHandler resourceHandler = new ResourceHandler("empty-template");

    public GeneratorCli() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GeneratorCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (vcpkgPath == null) {
            String vcpkgRootStr = System.getenv("VCPKG_ROOT");
            vcpkgPath = Paths.get(vcpkgRootStr);
        }

        if (projectDir.exists() || !projectDir.mkdir()) {
            System.out.println("Failed to create project folder");
            return 1;
        }

        CMakeBuilder cmakeBuilder = new CMakeBuilder(projectDir, resourceHandler)
                .withDescription(description)
                .addVcpkgDependency("fmt");
        cmakeBuilder.buildFiles();

        var srcDir = Util.createFolder(projectDir, "src");
        Util.createFolder(projectDir, "data");

        resourceHandler.copyResourceTo("main.cpp", srcDir);

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

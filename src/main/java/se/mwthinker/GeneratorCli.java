package se.mwthinker;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.Properties;

@SuppressWarnings("SpellCheckingInspection")
@Command(name = "cppgen",
        mixinStandardHelpOptions = true,
        description = "C++ generator using CMake",
        versionProvider = se.mwthinker.GeneratorCli.class
)
public class GeneratorCli implements Callable<Integer>, CommandLine.IVersionProvider {
    @Option(names = { "-n", "--new" }, required = true, paramLabel = "NEW", description = "The project name.")
    private File projectDir;

    @Option(names = { "-d", "--description" }, paramLabel = "DESCRIPTION", description = "Short description set in CMakeLists.txt.")
    private String description = "Description";

    @Option(names = { "-v", "--verbose" }, paramLabel = "VERBOSE", description = "Show verbose output.")
    private boolean verbose = false;

    @Option(names = { "-c", "--cmake" }, paramLabel = "OPEN", description = "Run cmake.")
    private boolean cmake = false;

    @Option(names = { "-o", "--open" }, paramLabel = "OPEN", description = "Open visual studio solution.")
    private boolean open = false;

    @Option(names = { "-g", "--gui" }, paramLabel = "GUI", description = "Add gui library.")
    private boolean gui = false;

    @Option(names = { "-t", "--test" }, paramLabel = "TEST", description = "Add test.")
    private boolean test = false;

    @Option(names = { "-l", "--license" }, paramLabel = "LICENSE", description = "Add MIT license with author.")
    private String author = "";

    public GeneratorCli() {
    }

    public static void main(String[] args) {
        var commandLine = new CommandLine(new GeneratorCli());
        try {
            commandLine.parseArgs(args);
        } catch (CommandLine.MissingParameterException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        commandLine.usage(System.out);
        if (commandLine.isUsageHelpRequested()) {
            System.exit(0);
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(commandLine.getOut());
            int exitCode = commandLine.getCommandSpec().exitCodeOnVersionHelp();
            System.exit(exitCode);
        } else {
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        }
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

        System.out.println("projectDir: " + projectDir.getName());
        FileSystem fileSystem = new FileSystem(projectDir, createResourceHandler());
        fileSystem.setVerbose(verbose);

        CMakeBuilder cmakeBuilder = new CMakeBuilder(fileSystem, new Github())
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
            File buildDir = new File(projectDir, "build");
            buildDir.mkdir();
            CMake.setVerbose(verbose);
            CMake.generate(projectDir, buildDir);
            if (open) {
                CMake.openVisualStudio(projectDir, buildDir);
            }
        }

        return 0;
    }

    @Override
    public String[] getVersion() throws Exception {
        final Properties properties = new Properties();
        properties.load(GeneratorCli.class.getClassLoader().getResourceAsStream("cppgen.properties"));
        return new String[]{"", "Version info: v" + properties.getProperty("version")};
    }

}

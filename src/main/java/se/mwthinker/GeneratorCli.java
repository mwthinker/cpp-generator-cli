package se.mwthinker;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.Properties;

@SuppressWarnings("SpellCheckingInspection")
@Command(name = "cppgen",
        mixinStandardHelpOptions = true,
        description = "C++ generator using CMake"
)
public class GeneratorCli implements Closeable, Flushable {
    @Parameters(index = "0", paramLabel = "PROJECT_NAME", description = "The project name.", arity = "0..1")
    private File projectDir;

    @Option(names = { "-d", "--description" }, paramLabel = "DESCRIPTION", description = "Short description set in CMakeLists.txt.")
    private String description = "Description";

    @Option(names = { "-V", "--verbose" }, paramLabel = "VERBOSE", description = "Show verbose output.")
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

    @Option(names = { "-k", "--keepFiles" }, paramLabel = "KEEPFILES", description = "Keep generated files on error.")
    private boolean keepFiles = false;

    @Option(names = { "-v", "--version" }, versionHelp = true, description = "Display version info.")
    private boolean versionRequested = false;

    private final ConsoleIO consoleIO;

    static void main(String[] args) {
        try (GeneratorCli generatorCli = new GeneratorCli()) {
            generatorCli.run(args);
        } catch (UserInterruptException | EndOfFileException _) {
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GeneratorCli() throws IOException {
        consoleIO = new ConsoleIO();
    }

    public void run(String[] args) {
        var commandLine = new CommandLine(this);
        try {
            commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            consoleIO.printError("Argument error: " + e.getMessage());
            System.exit(2);
        }

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        } else if (versionRequested) {
            printVersion();
            System.exit(0);
        }

        if (projectDir == null) {
            interactivePrompt(consoleIO);
        }
        int exitCode = executeGeneratorLogic();
        if (exitCode == 2) {
            commandLine.usage(System.out);
        }
        System.exit(exitCode);
    }

    private void interactivePrompt(ConsoleIO consoleIO) {
        String projectName = "";
        while (projectName.trim().isEmpty()) {
            projectName = consoleIO.askQuestion("Enter project name: ");
            if (projectName.trim().isEmpty()) {
                consoleIO.printError("Project name cannot be empty.");
            }
        }
        projectDir = new File(projectName.trim());

        String desc = consoleIO.askQuestion( "Enter project description ", "(or press Enter for default 'Description')");
        if (desc != null && !desc.trim().isEmpty()) {
            description = desc.trim();
        }

        gui = consoleIO.askYesNoQuestion("Add GUI library?");
        test = consoleIO.askYesNoQuestion("Add tests?");

        String authorInput = consoleIO.askQuestion("Enter author name for MIT license ", "(or press Enter to skip)");
        if (authorInput != null && !authorInput.trim().isEmpty()) {
            author = authorInput.trim();
        }

        cmake = consoleIO.askYesNoQuestion("Run cmake after generation?");
        if (cmake) {
            open = consoleIO.askYesNoQuestion("Open Visual Studio solution?");
        }

        verbose = consoleIO.askYesNoQuestion("Show verbose output?");
    }

    private int executeGeneratorLogic() {
        if (projectDir.exists() || !projectDir.mkdir()) {
            consoleIO.printError("Failed to create project folder: " + projectDir.getAbsolutePath());
            return 1;
        }
        if (!new File(projectDir, "data").mkdir()) {
            consoleIO.printError("Failed to create data folder");
            return 1;
        }

        consoleIO.printError("Generating project in: " + projectDir.getName());
        FileSystem fileSystem = new FileSystem(projectDir, createResourceHandler());
        fileSystem.setVerbose(verbose);

        CMakeBuilder cmakeBuilder = new CMakeBuilder(fileSystem, new Github())
                .withDescription(description)
                .withTestProject(test)
                .withLicense(LicenseType.MIT, author);

        if (gui) {
            cmakeBuilder
                    .addLinkLibrary("CppSdl3::CppSdl3")
                    .addSource("src/main.cpp")
                    .addSource("src/testwindow.cpp")
                    .addSource("src/testwindow.h")
                    .addVcpkgDependency("cppsdl3")
                    .addRegistry("mwthinker", "mw-vcpkg-registry", "cppsdl3");
        } else {
            cmakeBuilder
                    .addSource("src/main.cpp")
                    .addVcpkgDependency("fmt")
                    .addLinkLibrary("fmt::fmt");
        }
        try {
            cmakeBuilder.buildFiles();
        } catch (RuntimeException e) {
            if (!keepFiles) {
                consoleIO.printError("Cleaning up generated files due to error.");
                fileSystem.deleteProjectDir();
            }
            consoleIO.printError("Error during project generation: " + e.getMessage());
            return 1;
        }

        if (cmake || open) {
            File buildDir = new File(projectDir, "build");
            buildDir.mkdir();
            CMake.setVerbose(verbose);
            CMake.generate(projectDir, buildDir);
            if (open) {
                CMake.openVisualStudio(projectDir, buildDir);
            }
        }

        consoleIO.printError("Project generation complete.");
        return 0;
    }

    private ResourceHandler createResourceHandler() {
        if (gui) {
            return new ResourceHandler("gui-template");
        }
        return new ResourceHandler("empty-template");
    }

    public void printVersion() {
        Properties properties = new Properties();
        try {
            properties.load(GeneratorCli.class.getClassLoader().getResourceAsStream("cppgen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        consoleIO.printInfo("Version info: v" + properties.getProperty("version"));
    }

    @Override
    public void close() throws IOException {
        consoleIO.close();
    }

    @Override
    public void flush() throws IOException {
        consoleIO.flush();
    }

}

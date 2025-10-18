package se.mwthinker;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.Callable;

@SuppressWarnings("SpellCheckingInspection")
@Command(name = "cppgen",
        mixinStandardHelpOptions = true,
        description = "C++ generator using CMake",
        versionProvider = se.mwthinker.GeneratorCli.class
)
public class GeneratorCli implements Callable<Integer>, CommandLine.IVersionProvider {
    @Parameters(index = "0", paramLabel = "PROJECT_NAME", description = "The project name.", arity = "0..1")
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

    @Option(names = { "-f", "--useFetch" }, paramLabel = "GUI", description = "Use CMake FetchContent (instead .")
    private boolean fetch = false;

    @Option(names = { "-t", "--test" }, paramLabel = "TEST", description = "Add test.")
    private boolean test = false;

    @Option(names = { "-l", "--license" }, paramLabel = "LICENSE", description = "Add MIT license with author.")
    private String author = "";

    @Option(names = { "-k", "--keepFiles" }, paramLabel = "KEEPFILES", description = "Keep generated files on error.")
    private boolean keepFiles = false;

    static void main(String[] args) {
        var commandLine = new CommandLine(new GeneratorCli());
        try {
            commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            System.out.println("Argument error: " + e.getMessage());
            System.exit(2);
        }

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.usage(System.out);
            commandLine.printVersionHelp(commandLine.getOut());
            int exitCode = commandLine.getCommandSpec().exitCodeOnVersionHelp();
            System.exit(exitCode);
        } else {
            int exitCode = commandLine.execute(args);
            if (exitCode == 2) {
                commandLine.usage(System.out);
            }
            System.exit(exitCode);
        }
    }

    @Override
    public Integer call() {
        try {
            if (projectDir == null) {
                return runInteractive();
            } else {
                return executeGeneratorLogic();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private int runInteractive() throws IOException {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            PrintWriter writer = terminal.writer();
            askQuestions(lineReader, writer);
        }
        return executeGeneratorLogic();
    }

    private void askQuestions(LineReader lineReader, PrintWriter writer) {
        String projectName = "";
        while (projectName.trim().isEmpty()) {
            projectName = lineReader.readLine("Enter project name: ");
            if (projectName.trim().isEmpty()) {
                writer.println("Project name cannot be empty.");
            }
        }
        projectDir = new File(projectName.trim());

        String desc = lineReader.readLine("Enter project description (or press Enter for default 'Description'): ");
        if (desc != null && !desc.trim().isEmpty()) {
            description = desc.trim();
        }

        gui = askYesNoQuestion(lineReader, "Add GUI library? (y/N): ");
        if (gui) {
            fetch = askYesNoQuestion(lineReader, "Use CMake FetchContent instead of vcpkg? (y/N): ");
        }

        test = askYesNoQuestion(lineReader, "Add tests? (y/N): ");

        String authorInput = lineReader.readLine("Enter author name for MIT license (or press Enter to skip): ");
        if (authorInput != null && !authorInput.trim().isEmpty()) {
            author = authorInput.trim();
        }

        cmake = askYesNoQuestion(lineReader, "Run cmake after generation? (y/N): ");
        if (cmake) {
            open = askYesNoQuestion(lineReader, "Open Visual Studio solution? (y/N): ");
        }

        verbose = askYesNoQuestion(lineReader, "Show verbose output? (y/N): ");
    }

    private boolean askYesNoQuestion(LineReader reader, String prompt) {
        String input = reader.readLine(prompt);
        return input.trim().equalsIgnoreCase("y") || input.trim().equalsIgnoreCase("yes");
    }

    private int executeGeneratorLogic() {
        if (projectDir.exists() || !projectDir.mkdir()) {
            System.out.println("Failed to create project folder: " + projectDir.getAbsolutePath());
            return 1;
        }
        if (!new File(projectDir, "data").mkdir()) {
            System.out.println("Failed to create data folder");
            return 1;
        }

        System.out.println("Generating project in: " + projectDir.getName());
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
                    .addSource("src/testwindow.h");
            if (fetch) {
                cmakeBuilder
                        .addExternalProjectsWithDependencies("mwthinker", "CppSdl3");
            } else {
                cmakeBuilder
                        .addVcpkgDependency("cppsdl3")
                        .addRegistry("mwthinker", "mw-vcpkg-registry", "cppsdl3");
            }
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
                System.out.println("Cleaning up generated files due to error.");
                fileSystem.deleteProjectDir();
            }
            System.out.println("Error during project generation: " + e.getMessage());
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

        System.out.println("Project generation complete.");
        return 0;
    }

    private ResourceHandler createResourceHandler() {
        if (gui) {
            return new ResourceHandler("gui-template");
        }
        return new ResourceHandler("empty-template");
    }

    @Override
    public String[] getVersion() throws IOException {
        final Properties properties = new Properties();
        properties.load(GeneratorCli.class.getClassLoader().getResourceAsStream("cppgen.properties"));
        return new String[]{"", "Version info: v" + properties.getProperty("version")};
    }
}

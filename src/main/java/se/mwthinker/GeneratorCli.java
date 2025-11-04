package se.mwthinker;

import org.jline.consoleui.elements.ConfirmChoice;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings({"SpellCheckingInspection", "java:S106"})
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
    private String licenseAuthor = "";

    @Option(names = { "-k", "--keepFiles" }, paramLabel = "KEEPFILES", description = "Keep generated files on error.")
    private boolean keepFiles = false;

    @Option(names = { "-v", "--version" }, versionHelp = true, description = "Display version info.")
    private boolean versionRequested = false;

    private Terminal terminal;

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
        terminal = null;
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String[] args) {
        ConsolePrompt prompt = new ConsolePrompt(terminal);
        PromptBuilder builder = prompt.getPromptBuilder();

        var commandLine = new CommandLine(this);
        try {
            commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            builder.createText()
                    .addLine("Argument error: " + e.getMessage())
                    .addPrompt();
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
            interactivePrompt(prompt, builder);
        }
        int exitCode = executeGeneratorLogic();
        if (exitCode == 2) {
            commandLine.usage(System.out);
        }
        System.exit(exitCode);
    }

    private void interactivePrompt(ConsolePrompt prompt, PromptBuilder builder) {
        builder.createInputPrompt()
                .name("projectName")
                .message("Enter project name: ")
                .addPrompt()
                .createInputPrompt()
                .name("description")
                .message("Enter project description (or press Enter for default 'Description'): ")
                .defaultValue("Description")
                .addPrompt()
                .createConfirmPromp()
                .name("gui")
                .message("Add GUI library?")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt()
                .createConfirmPromp()
                .name("test")
                .message("Add tests?")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt()
                .createInputPrompt()
                .name("licenseAuthor")
                .defaultValue("")
                .message("Enter author name for MIT license (or press Enter to skip): ")
                .addPrompt()
                .createConfirmPromp()
                .name("cmake")
                .message("Run cmake after generation?")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt()
                .createConfirmPromp()
                .name("open")
                .message("Open Visual Studio solution?")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt()
                .createConfirmPromp()
                .name("verbose")
                .message("Show verbose output?")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt();

        Map<String, PromptResultItemIF> result;
        try {
            result = prompt.prompt(builder.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        projectDir = new File(result.get("projectName").toString());
        description = result.get("description").toString();
        gui = result.get("gui").toString().equalsIgnoreCase("yes");
        test = result.get("test").toString().equalsIgnoreCase("yes");
        licenseAuthor = result.get("licenseAuthor").toString();
        cmake = result.get("cmake").toString().equalsIgnoreCase("yes");
        open = result.get("open").toString().equalsIgnoreCase("yes");
        verbose = result.get("verbose").toString().equalsIgnoreCase("yes");
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
                .withLicense(LicenseType.MIT, licenseAuthor);

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

    public void printVersion() {
        Properties properties = new Properties();
        try {
            properties.load(GeneratorCli.class.getClassLoader().getResourceAsStream("cppgen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Version info: v" + properties.getProperty("version"));
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }

    @Override
    public void flush() throws IOException {
        terminal.flush();
    }

}

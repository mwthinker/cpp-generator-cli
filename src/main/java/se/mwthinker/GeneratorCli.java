package se.mwthinker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "cpp", mixinStandardHelpOptions = true, description = "Solves the 2021 05-problem")
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

    private final ResourceHandler resourceHandler = new ResourceHandler("empty-template");

    public GeneratorCli() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GeneratorCli()).execute(args);
        System.exit(exitCode);
    }

    private void saveToFile(File file, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        String text = resourceHandler.resourceAsString("CMakeLists.txt")
                .replace("NewProject", projectDir.getName())
                .replace("NewDescription", description);
        saveToFile(new File(projectDir, "CMakeLists.txt"), text);

        var srcDir = new File(projectDir, "src");
        srcDir.mkdir();

        var dataDir = new File(projectDir, "data");
        dataDir.mkdir();

        resourceHandler.copyResourceTo("main.cpp", srcDir);
        resourceHandler.copyResourceTo("CMakePresets.json", projectDir);

        saveVcpkgJson(projectDir);

        File buildDir = new File(projectDir, "build");

        if (cmake || open) {
            CMake.setVerbose(verbose);
            CMake.generate(projectDir, buildDir);
            if (open) {
                CMake.openVisualStudio(projectDir, buildDir);
            }
        }

        return 0;
    }

    private void saveVcpkgJson(File projectDir) {
        JSONObject object = resourceHandler.resourceAsJson("vcpkg.json");
        object.replace("name", projectDir.getName().toLowerCase());
        object.replace("description", description);
        var dependencies = (JSONArray) object.get("dependencies");
        dependencies.add("fmt");

        saveToFile(new File(projectDir, "vcpkg.json"), object.toJSONString());
    }

}

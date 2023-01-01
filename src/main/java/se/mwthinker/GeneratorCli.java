package se.mwthinker;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.SystemUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "cpp", mixinStandardHelpOptions = true, description = "Solves the 2021 05-problem")
public class GeneratorCli implements Callable<Integer> {

    @Option(names = { "-n", "--new" }, required = true, paramLabel = "NEW", description = "the project name")
    private File projectDir;

    @Option(names = { "-v", "--vcpkg" }, paramLabel = "VCPKG_ROOT", description = "the directory containing the vcpkg repository (overrides the env variable)")
    private Path vcpkgPath;

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

    private void copyResourceTo(String resource, File destDir) {
        try {
            var resourceFile = new File(ClassLoader.getSystemResource(resource).toURI());
            Files.copy(resourceFile.toPath(), new File(destDir, resourceFile.getName()).toPath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String resourceAsString(String resource) {
        try {
            return new String(getClass().getClassLoader().getResourceAsStream(resource).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject resourceAsJson(String resource) {
        try {
            var resourceFile = new File(ClassLoader.getSystemResource(resource).toURI());
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new FileReader(resourceFile));
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer call() throws Exception {
        if (vcpkgPath == null) {
            String vcpkgRootStr = System.getenv("VCPKG_ROOT");
            vcpkgPath = Paths.get(vcpkgRootStr);
        }

        if (projectDir.exists() || !projectDir.mkdir()) {
            System.out.println("Failed to create project folder");
            return 1;
        }

        String text = resourceAsString("empty-template/CMakeLists.txt");
        text = text.replace("NewProject", projectDir.getName());
        saveToFile(new File(projectDir, "CMakeLists.txt"), text);

        var srcDir = new File(projectDir, "src");
        srcDir.mkdir();

        var dataDir = new File(projectDir, "data");
        dataDir.mkdir();

        copyResourceTo("empty-template/main.cpp", srcDir);
        copyResourceTo("empty-template/CMakePresets.json", projectDir);

        JSONObject object = resourceAsJson("empty-template/vcpkg.json");
        object.replace("name", projectDir.getName().toLowerCase());
        var dependencies = (JSONArray) object.get("dependencies");
        dependencies.add("fmt");
        saveToFile(new File(projectDir, "vcpkg.json"), object.toJSONString());

        File buildDir = new File(projectDir, "build");

        String preset = "unix";
        if (SystemUtils.IS_OS_WINDOWS) {
            preset = "windows";
        }

        var cmdLine = org.apache.commons.exec.CommandLine.parse("cmake --preset " + preset + " -B \"" + buildDir.getAbsolutePath() +"\"");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectDir);
        int exitValue = executor.execute(cmdLine);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(20000);
        executor.setWatchdog(watchdog);

        executor.setWorkingDirectory(buildDir);
        String openVisualStudioSolution = "cmd /C start devenv \"" + projectDir.getName() + ".sln\"";
        System.out.println(openVisualStudioSolution);
        cmdLine = org.apache.commons.exec.CommandLine.parse(openVisualStudioSolution);
        exitValue = executor.execute(cmdLine);

        return 0;
    }

}

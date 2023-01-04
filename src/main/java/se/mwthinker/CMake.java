package se.mwthinker;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

abstract class CMake {
    static private boolean verbose = false;

    public static void setVerbose(boolean verbose) {
        CMake.verbose = verbose;
    }

    public static void generate(File projectDir, File buildDir) {
        var cmdLine = CommandLine.parse("cmake --preset " + getPreset() + " -B \"" + buildDir.getAbsolutePath() +"\"");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectDir);

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void openVisualStudio(File projectDir, File buildDir) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return;
        }

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(buildDir);

        String openVisualStudioSolution = "cmd /C start devenv \"" + projectDir.getName() + ".sln\"";
        if (verbose) {
            System.out.println(openVisualStudioSolution);
        }
        try {
            var cmdLine = CommandLine.parse(openVisualStudioSolution);
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPreset() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "windows";
        }
        return "unix";
    }
}

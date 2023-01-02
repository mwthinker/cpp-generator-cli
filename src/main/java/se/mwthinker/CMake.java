package se.mwthinker;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

abstract class CMake {
    static final private int WATCHDOG_TIME_MS = 20000;

    static private boolean verbose = false;

    static void setVerbose(boolean verbose) {
        CMake.verbose = verbose;
    }

    static public void generate(File projectDir, File buildDir) {
        var cmdLine = CommandLine.parse("cmake --preset " + getPreset() + " -B \"" + buildDir.getAbsolutePath() +"\"");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectDir);
        executor.setWatchdog(new ExecuteWatchdog(WATCHDOG_TIME_MS));

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static public void openVisualStudio(File projectDir, File buildDir) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return;
        }

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(buildDir);
        executor.setWatchdog(new ExecuteWatchdog(WATCHDOG_TIME_MS));

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

    static private String getPreset() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "windows";
        }
        return "unix";
    }

}

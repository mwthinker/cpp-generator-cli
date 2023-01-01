package se.mwthinker;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

public class CMakeGenerator {

    public CMakeGenerator() {
    }

    public int generate(File projectDir, File buildDir) {
        String preset = "unix";
        if (SystemUtils.IS_OS_WINDOWS) {
            preset = "windows";
        }

        var cmdLine = CommandLine.parse("cmake --preset " + preset + " -B \"" + buildDir.getAbsolutePath() +"\"");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectDir);
        executor.setWatchdog(new ExecuteWatchdog(20000));

        try {
            return executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int openVisualStudio(File projectDir, File buildDir) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return 0;
        }

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(buildDir);
        executor.setWatchdog(new ExecuteWatchdog(20000));

        String openVisualStudioSolution = "cmd /C start devenv \"" + projectDir.getName() + ".sln\"";
        System.out.println(openVisualStudioSolution);
        try {
            var cmdLine = CommandLine.parse(openVisualStudioSolution);
            return executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

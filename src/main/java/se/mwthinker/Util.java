package se.mwthinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

abstract class Util {

    public static void saveToFile(File file, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createFolder(File parent, String folder) {
        var newDir = new File(parent, folder);
        if (!newDir.mkdir()) {
            throw new RuntimeException();
        }
        return newDir;
    }

}

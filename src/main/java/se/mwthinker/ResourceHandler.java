package se.mwthinker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ResourceHandler {
    private final String templateDir;

    public ResourceHandler(String templateDir) {
        this.templateDir = templateDir;
    }

    public String resourceAsString(String resource) {
        try (var inputStream = getSystemResourceInputStream(resource)) {
            if (inputStream == null) {
                throw new RuntimeException();
            }
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyResourceTo(String resource, File destDir) {
        try {
            var inputStream = getSystemResourceInputStream(resource);
            Files.copy(inputStream, new File(destDir, resource).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getSystemResourceInputStream(String resource) {
       var stream = getClass().getClassLoader().getResourceAsStream(templateDir + "/" + resource);
       if (stream == null) {
           stream = getClass().getClassLoader().getResourceAsStream(resource);
       }
       return stream;
    }
}

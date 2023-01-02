package se.mwthinker;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class ResourceHandler {
    private final String templateDir;

    public ResourceHandler(String templateDir) {
        this.templateDir = templateDir;
    }

    public String resourceAsString(String resource) {
        try (var inputStreamer = getClass().getClassLoader().getResourceAsStream(getSystemResourceStr(resource))) {
            if (inputStreamer == null) {
                throw new RuntimeException();
            }
            return new String(inputStreamer.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyResourceTo(String resource, File destDir) {
        try {
            var resourceFile = getSystemResourceFile(resource);
            Files.copy(resourceFile.toPath(), new File(destDir, resourceFile.getName()).toPath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject resourceAsJson(String resource) {
        try {
            var resourceFile = getSystemResourceFile(resource);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new FileReader(resourceFile.getName()));
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private File getSystemResourceFile(String resource) throws URISyntaxException {
        return new File(ClassLoader.getSystemResource(getSystemResourceStr(resource)).toURI());
    }

    private String getSystemResourceStr(String resource) {
        return templateDir + "/" + resource;
    }
}

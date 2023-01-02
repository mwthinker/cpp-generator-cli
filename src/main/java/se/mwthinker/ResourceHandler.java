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
    private String templateDir;

    public ResourceHandler(String templateDir) {
        this.templateDir = templateDir;
    }

    public String resourceAsString(String resource) {
        try {
            return new String(getClass().getClassLoader().getResourceAsStream(templateDir + "/" + resource).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyResourceTo(String resource, File destDir) {
        try {
            var resourceFile = new File(ClassLoader.getSystemResource(templateDir + "/" + resource).toURI());
            Files.copy(resourceFile.toPath(), new File(destDir, resourceFile.getName()).toPath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject resourceAsJson(String resource) {
        try {
            var resourceFile = new File(ClassLoader.getSystemResource(templateDir + "/" + resource).toURI());
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new FileReader(resourceFile));
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

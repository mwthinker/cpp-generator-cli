package se.mwthinker;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

class FileSystem {

    private final ResourceHandler resourceHandler;

    public FileSystem(ResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    public File createFolder(File parent, String folder) {
        var newDir = new File(parent, folder);
        newDir.mkdirs();
        return newDir;
    }

    public File createFile(File parent, String file) {
        var newFile = new File(parent, file);
        newFile.getParentFile().mkdirs();
        return newFile;
    }

    public void copyResourceTo(String resource, File destDir) {
        resourceHandler.copyResourceTo(resource, destDir);
    }

    public void saveFileFromTemplate(Map<String, Object> data, String templateName, File saveToFile) {
        try (FileWriter writer = new FileWriter(saveToFile)) {
            resourceHandler
                    .getTemplate(templateName)
                    .process(data, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

}

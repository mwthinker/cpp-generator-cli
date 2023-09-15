package se.mwthinker;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

class FileSystem {
    private final File projectDir;
    private final ResourceHandler resourceHandler;

    public FileSystem(File projectDir, ResourceHandler resourceHandler) {
        this.projectDir = projectDir;
        this.resourceHandler = resourceHandler;
    }

    public String getProjectName() {
        return projectDir.getName();
    }

    public void copyResourceTo(String resource, String destName) {
        resourceHandler.copyResourceTo(resource, new File(projectDir, destName));
    }

    public void copyResourceTo(String resource) {
        copyResourceTo(getFileName(resource), resource);
    }

    public void saveToFile(VcpkgObject vcpkgObject, String saveToFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(
                    new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed("\n"))
            );
            writer.writeValue(new File(projectDir, saveToFile), vcpkgObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFileFromTemplate(Map<String, Object> data, String templateFileName,String saveToFile) {
        File file = new File(projectDir, saveToFile);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            resourceHandler
                    .getTemplate(templateFileName)
                    .process(data, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFileFromTemplate(Map<String, Object> data, String saveToFile) {
        saveFileFromTemplate(data, getTemplateFileName(saveToFile), saveToFile);
    }

    private String getFileName(String path) {
        if (path.isEmpty()) {
            throw new RuntimeException("Path is empty");
        }

        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String getTemplateFileName(String path) {
        String fileName = getFileName(path);
        return fileName.substring(0, fileName.lastIndexOf('.')) + ".ftl";
    }

}

package se.mwthinker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CMakeBuilderTest {

    private CMakeBuilder cmakeBuilder;

    @Mock
    private FileSystem fileSystem;
    @Mock
    private Github github;
    @Mock
    private VcpkgObjectFactory vcpkgObjectFactory;
    @Mock
    private File projectFolder;

    private Map<String, File> files;

    @BeforeEach
    public void setUp() {
        cmakeBuilder = new CMakeBuilder(fileSystem, github, vcpkgObjectFactory);
        files = new HashMap<>();
    }

    @Test
    public void buildMustHaveAtLeastOneSourceFile() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");
        mockCreateFile("main.cpp");
        VcpkgObject vcpkgObject = mock(VcpkgObject.class);
        when(vcpkgObjectFactory.createVcpkgObject(any(), any())).thenReturn(vcpkgObject);

        // When
        cmakeBuilder
                .addSource("main.cpp")
                .buildFiles();

        // Then
        // No exception
    }

    @Test
    public void buildMissingSourceFile() {
        assertThatThrownBy(() -> cmakeBuilder
                .buildFiles())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void buildDefaultFiles() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");
        mockCreateFile("src/main.cpp");
        VcpkgObject vcpkgObject = mock(VcpkgObject.class);
        when(vcpkgObjectFactory.createVcpkgObject(any(), any())).thenReturn(vcpkgObject);

        File cmakeListsFile = mockCreateFile("CMakeLists.txt");
        File vcpkgJsonFile = mockCreateFile("vcpkg.json");

        File workflowsFolder = mockCreateFolder(".github/workflows");
        File ciYmlFile = mockCreateFile(workflowsFolder, "ci.yml");

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .buildFiles();

        // Then
        verify(fileSystem).copyResourceTo("gitattributes", ".gitattributes");
        verify(fileSystem).copyResourceTo("gitignore", ".gitignore");
        verify(fileSystem).saveFileFromTemplate(any(), eq("CMakeLists.ftl"), eq(cmakeListsFile));
        verify(fileSystem).copyResourceTo("CMakePresets.json");
        verify(vcpkgObject).saveToFile(vcpkgJsonFile);
        verify(fileSystem).saveFileFromTemplate(any(), eq("ci.ftl"), eq(ciYmlFile));
        verify(fileSystem).copyResourceTo("main.cpp", files.get("src"));
    }

    @Test
    public void buildTestProject() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");
        mockCreateFile("src/main.cpp");
        VcpkgObject vcpkgObject = mock(VcpkgObject.class);
        when(vcpkgObjectFactory.createVcpkgObject(any(), any())).thenReturn(vcpkgObject);

        mockCreateFile("vcpkg.json");
        File testFolder = mockCreateFolder("MyProject_Test");
        File srcFolder = mockCreateFolder(testFolder, "src");

        mockCreateFile("vcpkg.json");
        File testCMakeListsFile = mockCreateFile(testFolder, "CMakeLists.txt");
        mockCreateFile("CMakeLists.txt");

        File workflowsFolder = mockCreateFolder(".github/workflows");
        mockCreateFile(workflowsFolder, "ci.yml");

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .withTestProject(true)
                .buildFiles();

        // Then
        verify(fileSystem).copyResourceTo("tests.cpp", srcFolder);
        verify(fileSystem).saveFileFromTemplate(any(), eq("Test_CMakeLists.ftl"), eq(testCMakeListsFile));
    }

    private File mockCreateFile(String path) {
        var names = path.split("/");
        for (var name : names) {
            File file = mock(File.class);
            files.putIfAbsent(name, file);
            lenient().when(file.getName()).thenReturn(name);
        }
        var fileName = names[names.length - 1];
        if (names.length == 1) {
            lenient().when(files.get(fileName).getParentFile()).thenReturn(projectFolder);
        } else {
            lenient().when(files.get(fileName).getParentFile()).thenReturn(files.get(names[names.length - 2]));
        }
        when(fileSystem.createFile(path)).thenReturn(files.get(fileName));
        return files.get(fileName);
    }

    private File mockCreateFile(File dir, String path) {
        File file = files.computeIfAbsent(path, s -> mock(File.class));
        when(fileSystem.createFile(dir, path)).thenReturn(file);
        return file;
    }

    private File mockCreateFolder(String path) {
        File file = files.computeIfAbsent(path, s -> mock(File.class));
        when(fileSystem.createFolder(path)).thenReturn(file);
        return file;
    }

    private File mockCreateFolder(File dir, String path) {
        File file = files.computeIfAbsent(path, s -> mock(File.class));
        when(fileSystem.createFolder(dir, path)).thenReturn(file);
        return file;
    }

}

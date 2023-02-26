package se.mwthinker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CMakeBuilderTest {

    private CMakeBuilder cmakeBuilder;

    @Mock(strictness = Mock.Strictness.WARN)
    private FileSystem fileSystem;
    @Mock
    private Github github;

    @BeforeEach
    public void setUp() {
        cmakeBuilder = new CMakeBuilder(fileSystem, github);
    }

    @Test
    public void buildMustHaveAtLeastOneSourceFile() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");

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

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .buildFiles();

        // Then
        verify(fileSystem).copyResourceTo("gitattributes", ".gitattributes");
        verify(fileSystem).copyResourceTo("gitignore", ".gitignore");
        verify(fileSystem).saveFileFromTemplate(any(), eq("CMakeLists.ftl"), eq("CMakeLists.txt"));
        verify(fileSystem).copyResourceTo("CMakePresets.json");
        verify(fileSystem).saveToFile(any(), eq("vcpkg.json"));
        verify(fileSystem).saveFileFromTemplate(any(), eq("ci.ftl"), eq(".github/workflows/ci.yml"));
        verify(fileSystem).copyResourceTo("main.cpp", "src/main.cpp");

        verify(fileSystem, times(3)).copyResourceTo(any(), any());
        verify(fileSystem, times(1)).copyResourceTo(any());
        verify(fileSystem, times(2)).saveFileFromTemplate(any(), any(), any());
        verify(fileSystem, times(1)).saveToFile(any(), any());
    }

    @Test
    public void buildTestProject() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .withTestProject(true)
                .buildFiles();

        // Then
        verify(fileSystem).copyResourceTo("tests.cpp", "MyProject_Test/src/tests.cpp");
        verify(fileSystem).saveFileFromTemplate(any(), any(), eq("CMakeLists.txt"));
    }

}

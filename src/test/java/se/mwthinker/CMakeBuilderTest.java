package se.mwthinker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
        verify(fileSystem).saveFileFromTemplate(any(), eq("CMakeLists.txt"));
        verify(fileSystem).copyResourceTo("CMakePresets.json");
        verify(fileSystem).saveToFile(any(VcpkgObject.class), eq("vcpkg.json"));
        verify(fileSystem).saveToFile(any(VcpkgConfigurationObject.class), eq("vcpkg-configuration.json"));
        verify(fileSystem).saveFileFromTemplate(any(), eq(".github/workflows/ci.yml"));
        verify(fileSystem).copyResourceTo("src/main.cpp");

        verify(fileSystem, times(2)).copyResourceTo(any(), any());
        verify(fileSystem, times(2)).copyResourceTo(any());
        verify(fileSystem, times(2)).saveFileFromTemplate(any(), any());
        verify(fileSystem, times(1)).saveToFile(any(VcpkgObject.class), eq("vcpkg.json"));
        verify(fileSystem, times(1)).saveToFile(any(VcpkgConfigurationObject.class), eq("vcpkg-configuration.json"));
    }

    @Test
    public void buildProjectWithDependency() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");
        when(github.fetchLatestCommitSHA("microsoft", "vcpkg"))
                .thenReturn("COMMIT_SHA");

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .addVcpkgDependency("fmt")
                .buildFiles();

        // Then

        verify(fileSystem).saveToFile(argThat(argument -> {
            if (argument instanceof VcpkgObject vcpkg) {
                var dependencies = vcpkg.getDependencies();
                return dependencies.size() == 1 && dependencies.contains("fmt");
            }
            return false;
        }), eq("vcpkg.json"));

        verify(fileSystem).saveToFile(argThat(argument -> {
            if (argument instanceof VcpkgConfigurationObject vcpkgConfig) {
                var defaultRegistry = vcpkgConfig.getDefaultRegistry();
                return defaultRegistry != null && "git".equals(defaultRegistry.getKind())
                        && "COMMIT_SHA".equals(defaultRegistry.getBaseline())
                        && "https://github.com/microsoft/vcpkg.git".equals(defaultRegistry.getRepository());
            }
            return false;
        }), eq("vcpkg-configuration.json"));
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
        verify(fileSystem).copyResourceTo("MyProject_Test/src/tests.cpp");
        verify(fileSystem).saveFileFromTemplate(any(), eq("Test_CMakeLists.ftl"), eq("MyProject_Test/CMakeLists.txt"));
    }

    @Test
    public void buildWithLicenseFile() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");

        // When
        cmakeBuilder
                .addSource("src/main.cpp")
                .withLicense(LicenseType.MIT, "SOME_NAME")
                .buildFiles();

        // Then
        verify(fileSystem).saveFileFromTemplate(any(), eq("LICENSE"));
    }

}

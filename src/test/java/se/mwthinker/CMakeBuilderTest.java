package se.mwthinker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    @BeforeEach
    public void setUp() {
        cmakeBuilder = new CMakeBuilder(fileSystem, github, vcpkgObjectFactory);
    }

    @Test
    public void buildMustHaveAtLeastOneSourceFile() {
        // Given
        when(fileSystem.getProjectName()).thenReturn("MyProject");
        when(fileSystem.createFile("main.cpp")).thenReturn(new File("main.cpp"));
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

}

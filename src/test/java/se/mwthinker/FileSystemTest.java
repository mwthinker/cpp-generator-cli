package se.mwthinker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

@ExtendWith(MockitoExtension.class)
public class FileSystemTest {

    private FileSystem fileSystem;

    @Mock
    private File projectDir;

    @Mock
    private ResourceHandler resourceHandler;

    @BeforeEach()
    public void setUp() {
        fileSystem = new FileSystem(projectDir, resourceHandler);
    }

}

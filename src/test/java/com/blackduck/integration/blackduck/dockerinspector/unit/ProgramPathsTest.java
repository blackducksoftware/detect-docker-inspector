package com.blackduck.integration.blackduck.dockerinspector.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.blackduck.dockerinspector.ProcessId;
import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;
import com.blackduck.integration.blackduck.dockerinspector.testutils.TestUtils;

@ExtendWith(SpringExtension.class)
public class ProgramPathsTest {

    @Test
    void test() throws IllegalArgumentException, IOException {
        Config config = Mockito.mock(Config.class);
        ProcessId processId = Mockito.mock(ProcessId.class);
        Mockito.when(processId.addProcessIdToName(Mockito.anyString())).thenReturn("run_1");
        File installDir = TestUtils.createTempDirectory();
        String installDirPath = installDir.getAbsolutePath();
        Mockito.when(config.getWorkingDirPath()).thenReturn(installDirPath);
        Mockito.when(processId.addProcessIdToName(Mockito.anyString())).thenReturn("test");

        ProgramPaths programPaths = new ProgramPaths(config, processId);

        assertEquals(installDirPath, programPaths.getDockerInspectorPgmDirPath());
        String runDirPath = programPaths.getDockerInspectorRunDirPath();
        assertEquals(String.format("%sconfig/", runDirPath), programPaths.getDockerInspectorConfigDirPath());
        assertEquals(String.format("%sconfig/application.properties", runDirPath), programPaths.getDockerInspectorConfigFilePath());
        assertEquals(String.format("%starget/", runDirPath), programPaths.getDockerInspectorTargetDirPath());
    }
}

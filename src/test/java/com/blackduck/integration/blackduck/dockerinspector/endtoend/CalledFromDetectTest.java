package com.blackduck.integration.blackduck.dockerinspector.endtoend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;
import com.blackduck.integration.blackduck.dockerinspector.programversion.ProgramVersion;
import com.blackduck.integration.blackduck.dockerinspector.testutils.TestUtils;
import com.blackduck.integration.exception.IntegrationException;

@Tag("integration")
public class CalledFromDetectTest {
    // TODO change this
    private static final int DETECT_MAJOR_VERSION = 8;
    private static final String TEXT_PRECEDING_BDIO_FILE_DIR_PATH = "BDIO Generated: ";
    private static ProgramVersion programVersion;
    private static File executionDir;

    private static long ONE_MINUTE_IN_MS = 1L * 60L * 1000L;
    private static long FIVE_MINUTES_IN_MS = 5L * 60L * 1000L;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        programVersion = new ProgramVersion();
        programVersion.init();
        executionDir = Files.createTempDir();
        executionDir.deleteOnExit();
    }

    @Test
    public void test() throws IOException, InterruptedException, IntegrationException {

        String cmdGetDetectScriptString = String.format("curl --insecure -s https://detect.blackduck.com/detect%d.sh", DETECT_MAJOR_VERSION);
        String detectScriptString = TestUtils.execCmd(executionDir, cmdGetDetectScriptString, ONE_MINUTE_IN_MS, true, null);
        File detectScriptFile = File.createTempFile("latestDetect", ".sh");
        detectScriptFile.setExecutable(true);
        detectScriptFile.deleteOnExit();
        System.out.printf("script file: %s\n", detectScriptFile.getAbsolutePath());
        FileUtils.write(detectScriptFile, detectScriptString, StandardCharsets.UTF_8);

        File detectOutputFile = File.createTempFile("detectOutput", ".txt");
        detectOutputFile.setWritable(true);
        detectScriptFile.deleteOnExit();

        StringBuffer sb = new StringBuffer();
        sb.append("#\n");
        sb.append(detectScriptFile.getAbsolutePath());
        sb.append(String.format(" --detect.docker.inspector.path=%s/build/libs/detect-docker-inspector-%s.jar", System.getProperty("user.dir"), programVersion.getProgramVersion()));
        sb.append(" --blackduck.offline.mode=true");
        sb.append(" --detect.docker.image=alpine:latest");
        sb.append(" --detect.tools.excluded=SIGNATURE_SCAN");
        sb.append(" --detect.docker.path.required=false");
        sb.append(String.format(" --logging.level.detect=%s", "DEBUG"));
        sb.append(String.format(" --detect.docker.passthrough.cleanup.inspector.container=%b", true));
        // Port 9000 (default) may be occupied by system services. Use alternate ports.
        sb.append(" --detect.docker.passthrough.imageinspector.service.port.alpine=9100");
        sb.append(" --detect.docker.passthrough.imageinspector.service.port.centos=9101");
        sb.append(" --detect.docker.passthrough.imageinspector.service.port.ubuntu=9102");
        sb.append(String.format(" --detect.cleanup=%b", false));
        sb.append(String.format(" > %s", detectOutputFile.getAbsolutePath()));

        String detectWrapperScriptString = sb.toString();
        System.out.printf("Detect wrapper script content:\n%s\n", detectWrapperScriptString);
        File detectWrapperScriptFile = File.createTempFile("detectWrapper", ".sh");
        detectWrapperScriptFile.setExecutable(true);
        detectScriptFile.deleteOnExit();
        System.out.printf("script file: %s\n", detectWrapperScriptFile.getAbsolutePath());
        FileUtils.write(detectWrapperScriptFile, detectWrapperScriptString, StandardCharsets.UTF_8);
        Map<String, String> env = new HashMap<>();
        env.put("DETECT_CURL_OPTS", "--insecure");
        String wrapperScriptOutput = TestUtils.execCmd(executionDir, detectWrapperScriptFile.getAbsolutePath(), FIVE_MINUTES_IN_MS, true, env);
        System.out.printf("Wrapper script output (normally empty):\n%s\n", wrapperScriptOutput);
        String detectOutputString = FileUtils.readFileToString(detectOutputFile, StandardCharsets.UTF_8);
        System.out.printf("Detect output: %s", detectOutputString);

        File bdioFile = getBdioFile(detectOutputString);
        assertTrue(bdioFile.exists());
        String dockerInspectorBdioFileContents;
        if (bdioFile.getName().endsWith(".bdio")) {
            // Detect 8 generates BDIO2 format (.bdio zip). With --detect.cleanup=false the docker
            // inspector's BDIO1 jsonld is preserved in the extractions directory next to the bdio dir.
            File runDir = bdioFile.getParentFile().getParentFile(); // .../runs/<timestamp>/
            File dockerExtractionDir = new File(runDir, "extractions/DOCKER-0");
            File[] jsonldFiles = dockerExtractionDir.listFiles((dir, name) -> name.endsWith("_bdio.jsonld"));
            assertTrue(jsonldFiles != null && jsonldFiles.length > 0,
                    "Expected docker inspector jsonld file in " + dockerExtractionDir.getAbsolutePath());
            System.out.printf("Using docker inspector bdio jsonld: %s\n", jsonldFiles[0].getAbsolutePath());
            dockerInspectorBdioFileContents = FileUtils.readFileToString(jsonldFiles[0], StandardCharsets.UTF_8);
        } else {
            dockerInspectorBdioFileContents = FileUtils.readFileToString(bdioFile, StandardCharsets.UTF_8);
        }
        assertTrue(dockerInspectorBdioFileContents.contains("\"externalId\": \"alpine/latest\","));

        assertTrue(detectOutputString.contains("DOCKER: SUCCESS"));
        assertTrue(detectOutputString.contains("Overall Status: SUCCESS"));
    }

    private File getBdioFile(String detectOutputString) throws IntegrationException {
        String bdioFilePath = getBdioFilePath(detectOutputString);
        File bdioFile = new File(bdioFilePath);
        return bdioFile;
    }

    private String getBdioFilePath(String detectOutputString) throws IntegrationException {
        for (String line : detectOutputString.split("\n")) {
            if (line.matches(String.format(".*%s.*", TEXT_PRECEDING_BDIO_FILE_DIR_PATH))) {
                System.out.printf("found line: %s\n", line);
                int bdioFilePathStart = line.indexOf(TEXT_PRECEDING_BDIO_FILE_DIR_PATH) + TEXT_PRECEDING_BDIO_FILE_DIR_PATH.length();
                String bdioFilePath = line.substring(bdioFilePathStart);
                System.out.printf("BDIO file path: %s\n", bdioFilePath);
                return bdioFilePath;
            }
        }
        throw new IntegrationException("BDIO file path not found");
    }
}

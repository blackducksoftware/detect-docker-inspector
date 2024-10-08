package com.blackduck.integration.blackduck.dockerinspector.endtoend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.blackduck.dockerinspector.programversion.ProgramVersion;
import com.blackduck.integration.blackduck.dockerinspector.testutils.TestUtils;

@Tag("integration")
public class ImageCleanupTest {

    private static final String PROJECT_NAME = "Pro Ject";
    private static final String PROJECT_VERSION = "Ver Sion";
    private static final String TARGET_IMAGE_NAME = "alpine";
    private static final String INSPECTOR_IMAGE_SUFFIX = "alpine";
    private static final String TARGET_IMAGE_TAG = "2.6";

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        try {
            boolean created = new File(TestUtils.TEST_DIR_REL_PATH).mkdirs();
            System.out.println(String.format("test dir created: %b", created));
        } catch (Exception e) {
            System.out.println(String.format("mkdir %s: %s", TestUtils.TEST_DIR_REL_PATH, e.getMessage()));
        }
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws IOException, InterruptedException {
        String workingDirPath = String.format("%s/imageCleanup", TestUtils.TEST_DIR_REL_PATH);
        try {
            FileUtils.deleteDirectory(new File(workingDirPath));
        } catch (Exception e) {
            System.out.println(String.format("Unable to delete %s", workingDirPath));
        }

        ProgramVersion pgmVerObj = new ProgramVersion();
        pgmVerObj.init();

        // IFF inspector image is absent or can be removed: expect it to be gone at end
        boolean expectInspectOnImageRemoved = true;
        String inspectOnImageRepoName = String.format("blackducksoftware/%s-%s", pgmVerObj.getInspectorImageFamily(), INSPECTOR_IMAGE_SUFFIX);
        String inspectOnImageTag = pgmVerObj.getInspectorImageVersion();
        List<String> dockerImageList = getDockerImageList();
        if (isImagePresent(dockerImageList, inspectOnImageRepoName, inspectOnImageTag)) {
            String runOnImageRepoAndTag = String.format("%s:%s", inspectOnImageRepoName, inspectOnImageTag);
            System.out.printf("RunOn image %s exists locally; will try to remove it\n", runOnImageRepoAndTag);
            List<String> dockerRmiCmd = Arrays.asList("bash", "-c", String.format("docker rmi %s", runOnImageRepoAndTag));
            String log = runCommand(dockerRmiCmd, false);
            System.out.println(log);
            dockerImageList = getDockerImageList();
            if (isImagePresent(dockerImageList, inspectOnImageRepoName, inspectOnImageTag)) {
                System.out.printf("InspectOn Image %s already exists and can't be removed, so won't expect DI to remove it when finished\n", runOnImageRepoAndTag);
                expectInspectOnImageRemoved = false;
            }
        }

        String programVersion = pgmVerObj.getProgramVersion();
        List<String> partialCmd = Arrays.asList("java", "-jar", String.format("build/libs/detect-docker-inspector-%s.jar", programVersion),
            String.format("--bdio.project.name=\"%s\"", PROJECT_NAME),
            String.format("--bdio.project.version=\"%s\"", PROJECT_VERSION),
            String.format("--output.path=%s/output", TestUtils.TEST_DIR_REL_PATH),
            "--output.include.dockertarfile=true",
            "--output.include.containerfilesystem=true", "--include.target.image=true", "--include.inspector.image=true"
        );
        List<String> fullCmd = new ArrayList<>();
        fullCmd.addAll(partialCmd);
        fullCmd.add("--logging.level.detect=DEBUG");
        fullCmd.add("--cleanup.inspector.image=true");
        fullCmd.add("--cleanup.target.image=true");
        fullCmd.add(String.format("--working.dir.path=%s", workingDirPath));
        fullCmd.add(String.format("--docker.image=%s", TARGET_IMAGE_NAME, TARGET_IMAGE_TAG));
        fullCmd.add("--caller.name=Detect");
        String log = runCommand(fullCmd, true);
        System.out.println(log);
        Thread.sleep(10000L); // give docker a few seconds
        dockerImageList = getDockerImageList();
        if (expectInspectOnImageRemoved) {
            assertFalse(isImagePresent(dockerImageList, inspectOnImageRepoName, inspectOnImageTag));
        }
        assertFalse(isImagePresent(dockerImageList, TARGET_IMAGE_NAME, TARGET_IMAGE_TAG), String.format("Target image %s:%s was not removed", TARGET_IMAGE_NAME, TARGET_IMAGE_TAG));
    }

    private String runCommand(List<String> cmd, boolean assertPasses) throws IOException, InterruptedException {
        System.out.println(String.format("Running command %s", cmd.toString()));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        File outputFile = new File(String.format("%s/temp_cmd_output_%s.txt", TestUtils.TEST_DIR_REL_PATH, Long.toString(System.nanoTime())));
        outputFile.delete();
        pb.redirectErrorStream(true);
        pb.redirectOutput(outputFile);
        Process p = pb.start();
        int retCode = p.waitFor();
        String log = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
        System.out.println(log);
        if (assertPasses) {
            assertEquals(0, retCode);
        }
        outputFile.delete();
        return log;
    }

    private List<String> getDockerImageList() throws IOException, InterruptedException {
        List<String> dockerImagesCmd = new ArrayList<>();
        dockerImagesCmd.add("bash");
        dockerImagesCmd.add("-c");
        dockerImagesCmd.add("docker images");
        final String description = "dockerImages";

        return getCmdOutputLines(dockerImagesCmd, description);
    }

    private List<String> getCmdOutputLines(List<String> dockerImagesCmd, String description) throws IOException, InterruptedException {
        String outputFilename = String.format("%s/imageCleanup_%sOutput.txt", TestUtils.TEST_DIR_REL_PATH, description);
        System.out.println(String.format("Running command %s", dockerImagesCmd.toString()));
        File dockerImagesoutputFile = new File(outputFilename);
        dockerImagesoutputFile.delete();
        ProcessBuilder pb = new ProcessBuilder(dockerImagesCmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(dockerImagesoutputFile);
        Process p = pb.start();
        int retCode = p.waitFor();
        String dockerImagesCommandOutput = FileUtils.readFileToString(dockerImagesoutputFile, StandardCharsets.UTF_8);
        System.out.printf("%s: %s\n", description, dockerImagesCommandOutput);
        assertEquals(0, retCode);

        String[] linesArray = dockerImagesCommandOutput.split("\\r?\\n");
        List<String> linesList = Arrays.asList(linesArray);
        return linesList;
    }

    private boolean isImagePresent(List<String> dockerImageList, String targetImageName, String targetImageTag) {
        System.out.printf("Checking docker image list for image %s:%s\n", targetImageName, targetImageTag);
        String imageRegex = String.format("^%s +%s.*$", targetImageName, targetImageTag.replaceAll("\\.", "\\."));
        for (String imageListLine : dockerImageList) {
            if (imageListLine.matches(imageRegex)) {
                System.out.println("\tFound it");
                return true;
            }
        }
        System.out.println("\tDid not find it");
        return false;
    }
}

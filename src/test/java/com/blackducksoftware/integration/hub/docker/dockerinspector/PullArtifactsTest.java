package com.blackducksoftware.integration.hub.docker.dockerinspector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class PullArtifactsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testPullJar() throws IOException, InterruptedException, IntegrationException {
        testPullOperation("jar", "jar");
    }

    @Test
    public void testPullAirGapZip() throws IOException, InterruptedException, IntegrationException {
        testPullOperation("airgapzip", "zip");
    }

    private void testPullOperation(final String pullTarget, final String extension) throws IOException, InterruptedException, IntegrationException {
        final String cmdLineOption = String.format("--pull%s", pullTarget);
        final File workingDir = new File(String.format("%s/pull%s", TestUtils.TEST_DIR_REL_PATH, pullTarget));
        FileUtils.deleteDirectory(workingDir);
        workingDir.mkdirs();
        System.out.println(String.format("workingDir: %s", workingDir.getAbsolutePath()));
        final FilenameFilter fileFilter = getFilenameFilterByExtension(extension);
        final File[] filesBefore = workingDir.listFiles(fileFilter);
        assertTrue(String.format("%s should be an empty directory", workingDir.getAbsolutePath()), filesBefore.length == 0);

        final File script = new File("build/hub-docker-inspector.sh");
        final List<String> partialCmd = Arrays.asList(script.getAbsolutePath(), cmdLineOption);
        // Arrays.asList returns a fixed size list; need a variable sized list
        final List<String> fullCmd = new ArrayList<>();
        fullCmd.addAll(partialCmd);

        System.out.println(String.format("Running %s end to end test", cmdLineOption));
        TestUtils.execCmd(workingDir, String.join(" ", fullCmd), 30000L, true);
        System.out.printf("hub-docker-inspector %s done; verifying results...\n", cmdLineOption);

        final File[] filesAfter = workingDir.listFiles(fileFilter);
        final boolean foundOne = filesAfter.length == 1;
        for (final File f : filesAfter) {
            System.out.println(String.format("Found pulled file: %s", f.getName()));
            f.delete();
        }
        assertTrue("Expected a single pulled file", foundOne);
    }

    private FilenameFilter getFilenameFilterByExtension(final String extension) {
        final String dotExtension = String.format(".%s", extension);
        final FilenameFilter jarFileFilter = (dir, name) -> {
            if (name.endsWith(dotExtension)) {
                return true;
            } else {
                return false;
            }
        };
        return jarFileFilter;
    }
}

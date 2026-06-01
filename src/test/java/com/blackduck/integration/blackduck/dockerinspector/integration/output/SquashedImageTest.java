package com.blackduck.integration.blackduck.dockerinspector.integration.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.blackduck.dockerinspector.ProcessId;
import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;
import com.blackduck.integration.blackduck.dockerinspector.dockerclient.DockerClientManager;
import com.blackduck.integration.blackduck.dockerinspector.output.CompressedFile;
import com.blackduck.integration.blackduck.dockerinspector.output.ImageTarFilename;
import com.blackduck.integration.blackduck.dockerinspector.output.SquashedImage;
import com.blackduck.integration.blackduck.imageinspector.api.name.ImageNameResolver;
import com.blackduck.integration.blackduck.imageinspector.linux.FileOperations;
import com.blackduck.integration.exception.IntegrationException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Tag("integration")
class SquashedImageTest {
    private static SquashedImage squashedImage;
    private static DockerClientManager dockerClientManager;
    private static File testWorkingDir;

    @BeforeAll
    static void setUp() throws IOException {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        Logger integrationLogger = (Logger) LoggerFactory.getLogger("com.blackduck.integration");
        integrationLogger.setLevel(Level.DEBUG);

        testWorkingDir = new File("test/output/squashingTest");
        ImageTarFilename imageTarFilename = new ImageTarFilename();
        FileOperations fileOperations = new FileOperations();
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.getWorkingDirPath()).thenReturn(testWorkingDir.getCanonicalPath());
        ProgramPaths programPaths = new ProgramPaths(config, new ProcessId());
        dockerClientManager = new DockerClientManager(fileOperations, new ImageNameResolver(), config, imageTarFilename, programPaths);

        squashedImage = new SquashedImage();
        squashedImage.setDockerClientManager(dockerClientManager);
        squashedImage.setFileOperations(new FileOperations());
    }

    @Test
    void testCreateSquashedImageTarGz() throws IOException, IntegrationException {

        File targetImageFileSystemTarGz = new File("src/test/resources/test_containerfilesystem.tar.gz");

        FileUtils.deleteDirectory(testWorkingDir);
        File tempTarFile = new File(testWorkingDir, "tempContainerFileSystem.tar");
        File squashingWorkingDir = new File(testWorkingDir, "squashingCode");
        squashingWorkingDir.mkdirs();
        File squashedImageTarGz = new File("test/output/squashingTest/test_squashedimage.tar.gz");

        squashedImage.createSquashedImageTarGz(targetImageFileSystemTarGz, squashedImageTarGz, tempTarFile, squashingWorkingDir);

        File unpackedSquashedImageDir = new File(testWorkingDir, "squashedImageUnpacked");
        unpackedSquashedImageDir.mkdirs();
        CompressedFile.gunZipUnTarFile(squashedImageTarGz, tempTarFile, unpackedSquashedImageDir);

        File manifestFile = new File(unpackedSquashedImageDir, "manifest.json");
        assertTrue(manifestFile.isFile());

        // Find the layer tar: handles both classic Docker format and OCI format (Docker 25+).
        File layerTar = null;

        // Classic Docker format: look for a directory (not "blobs") containing a .tar file
        for (File imageFile : unpackedSquashedImageDir.listFiles()) {
            if (imageFile.isDirectory() && !imageFile.getName().equals("blobs")) {
                for (File layerFile : imageFile.listFiles()) {
                    if (layerFile.getName().endsWith(".tar")) {
                        layerTar = layerFile;
                        break;
                    }
                }
                if (layerTar != null) {
                    break;
                }
            }
        }

        // OCI format: blobs/sha256/ contains config blob (small JSON) + layer blob (large tar).
        // Pick the largest blob — that is the layer.
        if (layerTar == null) {
            File blobsDir = new File(unpackedSquashedImageDir, "blobs/sha256");
            if (blobsDir.isDirectory()) {
                Collection<File> blobs = FileUtils.listFiles(blobsDir, TrueFileFilter.TRUE, FalseFileFilter.FALSE);
                layerTar = blobs.stream().max(Comparator.comparingLong(File::length)).orElse(null);
            }
        }

        assertNotNull(layerTar, "Could not find layer tar in either classic Docker or OCI format");
        File layerUnpackedDir = new File(squashingWorkingDir, "squashedImageLayerUnpacked");
        CompressedFile.unTarFile(layerTar, layerUnpackedDir);

        // Verify that the symlink made it into the squashed image
        File symLink = new File(layerUnpackedDir, "usr/share/apk/keys/aarch64/alpine-devel@lists.alpinelinux.org-58199dcc.rsa.pub");
        assertTrue(symLink.exists());
        Path symLinkPath = symLink.toPath();
        assertTrue(Files.isSymbolicLink(symLinkPath));
        Path symLinkTargetPath = Files.readSymbolicLink(symLinkPath);
        assertEquals("../alpine-devel@lists.alpinelinux.org-58199dcc.rsa.pub", symLinkTargetPath.toString());
    }

    @Test
    void testGenerateUniqueImageRepoTag() throws IntegrationException {
        String generatedRepTag = squashedImage.generateUniqueImageRepoTag();

        assertTrue(generatedRepTag.startsWith("dockerinspectorsquashed-"));
        assertTrue(generatedRepTag.endsWith(":1"));
    }
}

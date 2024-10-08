package com.blackduck.integration.blackduck.dockerinspector.integration.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.Gson;
import com.blackduck.integration.bdio.model.BdioBillOfMaterials;
import com.blackduck.integration.bdio.model.BdioProject;
import com.blackduck.integration.bdio.model.SimpleBdioDocument;
import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;
import com.blackduck.integration.blackduck.dockerinspector.dockerclient.DockerClientManager;
import com.blackduck.integration.blackduck.dockerinspector.output.CompressedFile;
import com.blackduck.integration.blackduck.dockerinspector.output.ContainerFilesystemFilename;
import com.blackduck.integration.blackduck.dockerinspector.output.ImageTarFilename;
import com.blackduck.integration.blackduck.dockerinspector.output.Output;
import com.blackduck.integration.blackduck.dockerinspector.output.OutputFiles;
import com.blackduck.integration.blackduck.dockerinspector.output.SquashedImage;
import com.blackduck.integration.blackduck.imageinspector.api.name.ImageNameResolver;
import com.blackduck.integration.blackduck.imageinspector.linux.FileOperations;
import com.blackduck.integration.exception.IntegrationException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Tag("integration")
@ExtendWith(SpringExtension.class)
public class OutputTest {

    @Mock
    private Config config;

    @Mock
    private ProgramPaths programPaths;

    @Mock
    private Gson gson;

    @Mock
    private ContainerFilesystemFilename containerFilesystemFilename;

    @InjectMocks
    private Output output;

    private static File outputDir;
    private static File workingDir;
    private static File squashedImageTarfile;
    private static File squashingTempDir;

    @BeforeAll
    public static void setup() throws IOException {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        Logger integrationLogger = (Logger) LoggerFactory.getLogger("com.blackduck.integration");
        integrationLogger.setLevel(Level.DEBUG);

        File testHome = new File("test/output/squashedImageCreation");
        FileUtils.deleteDirectory(testHome);
        outputDir = new File(testHome, "out");
        outputDir.mkdirs();
        File containerFileSystemFrom = new File("src/test/resources/target_containerfilesystem.tar.gz");
        File containerFileSystemTo = new File(outputDir, "target_containerfilesystem.tar.gz");
        FileUtils.copyFile(containerFileSystemFrom, containerFileSystemTo);
        workingDir = new File(testHome, "working");
        workingDir.mkdirs();
        squashedImageTarfile = new File(workingDir, "target_squashedimage.tar");
        squashingTempDir = new File(workingDir, "squashing_tmp");
    }

    @Test
    public void testSquashedImageCreation() throws IOException, IntegrationException {

        Mockito.when(config.getOutputPath()).thenReturn(outputDir.getAbsolutePath());
        Mockito.when(config.isOutputIncludeSquashedImage()).thenReturn(true);
        Mockito.when(programPaths.getUserOutputDirPath()).thenReturn(outputDir.getAbsolutePath());
        Mockito.when(programPaths.getDockerInspectorWorkingOutputPath()).thenReturn(workingDir.getAbsolutePath());
        Mockito.when(programPaths.getDockerInspectorSquashedImageTarFilePath()).thenReturn(squashedImageTarfile.getAbsolutePath());
        Mockito.when(programPaths.getDockerInspectorSquashedImageDirPath()).thenReturn(squashingTempDir.getAbsolutePath());

        SimpleBdioDocument bdioDoc = Mockito.mock(SimpleBdioDocument.class);
        BdioBillOfMaterials bom = new BdioBillOfMaterials();
        bom.spdxName = "registry.luciddg.com_luciddg_ldg-server-qa_2020.16.03_DPKG";
        Mockito.when(bdioDoc.getBillOfMaterials()).thenReturn(bom);
        BdioProject project = new BdioProject();
        Mockito.when(bdioDoc.getProject()).thenReturn(project);
        Mockito.when(containerFilesystemFilename.deriveContainerFilesystemFilename(null, null)).thenReturn("target_containerfilesystem.tar.gz");

        ImageTarFilename imageTarFilename = new ImageTarFilename();
        FileOperations fileOperations = new FileOperations();
        DockerClientManager dockerClientManager = new DockerClientManager(fileOperations, new ImageNameResolver(), config, imageTarFilename, programPaths);
        SquashedImage squashedImage = new SquashedImage();
        squashedImage.setFileOperations(fileOperations);
        squashedImage.setDockerClientManager(dockerClientManager);
        output.setSquashedImage(squashedImage);

        // Test
        OutputFiles outputFiles = output.addOutputToFinalOutputDir(bdioDoc, null, null);

        // Verify
        File generatedSquashedImageCompressedFile = outputFiles.getSquashedImageFile();
        File generatedSquashedImageTarfile = new File(workingDir, "generatedImageTarfile");
        CompressedFile.gunZipFile(generatedSquashedImageCompressedFile, generatedSquashedImageTarfile);
        File generatedSquashedImageContents = new File(workingDir, "generatedSquashedImageContents");
        CompressedFile.unTarFile(generatedSquashedImageTarfile, generatedSquashedImageContents);
        System.out.println(String.format("Look in: %s", generatedSquashedImageContents.getAbsolutePath()));
        Collection<File> layerFiles = FileUtils.listFiles(generatedSquashedImageContents, new NameFileFilter("layer.tar"), TrueFileFilter.TRUE);
        assertEquals(1, layerFiles.size());
        File generatedLayer = new File(workingDir, "generatedLayer");
        CompressedFile.unTarFile(layerFiles.iterator().next(), generatedLayer);
        File expectedFile = new File(generatedLayer, "opt/luciddg-server/modules/django/bin/100_assets.csv");
        assertTrue(expectedFile.exists());
    }
}

package com.blackduck.integration.blackduck.dockerinspector.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.dockerinspector.config.DockerInspectorSystemProperties;
import com.synopsys.integration.exception.IntegrationException;

public class DockerInspectorSystemPropertiesTest {
    private static File propertiesFile;

    @BeforeAll
    public static void setUp() throws IOException {
        propertiesFile = new File("test/additionalSystemProperties.properties");
        FileUtils.writeStringToFile(propertiesFile, "testproperty=testvalue", StandardCharsets.UTF_8);
    }

    @Test
    public void testNothingAdded() throws IntegrationException {
        DockerInspectorSystemProperties propertyMgr = new DockerInspectorSystemProperties();

        Properties systemPropertiesBefore = System.getProperties();
        propertyMgr.augmentSystemProperties(null);
        Properties systemPropertiesAfter = System.getProperties();
        assertEquals(systemPropertiesAfter.size(), systemPropertiesBefore.size());
    }

    @Test
    public void testPropertyAdded() throws IntegrationException {
        DockerInspectorSystemProperties propertyMgr = new DockerInspectorSystemProperties();

        int propertyCountBefore = System.getProperties().size();
        System.out.printf("Before: #properties: %d\n", propertyCountBefore);
        assertEquals(null, System.getProperty("testproperty"));
        propertyMgr.augmentSystemProperties(propertiesFile.getAbsolutePath());
        int propertyCountAfter = System.getProperties().size();
        assertEquals("testvalue", System.getProperty("testproperty"));
        assertEquals(propertyCountBefore + 1, propertyCountAfter);
    }

}

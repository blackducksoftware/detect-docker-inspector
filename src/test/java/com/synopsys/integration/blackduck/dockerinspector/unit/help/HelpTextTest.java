package com.synopsys.integration.blackduck.dockerinspector.unit.help;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.blackduck.dockerinspector.config.Config;
import com.synopsys.integration.blackduck.dockerinspector.config.DockerInspectorOption;
import com.synopsys.integration.blackduck.dockerinspector.help.HelpText;

@ExtendWith(SpringExtension.class)
class HelpTextTest {

    @Mock
    private Config config;

    @InjectMocks
    private HelpText helpText;

    @Test
    void testProperties() throws IllegalArgumentException {
        SortedSet<DockerInspectorOption> configOptions = new TreeSet<>();
        configOptions.add(new DockerInspectorOption("blackduck.url", "testBlackDuckUrl", "Black Duck URL", String.class, "", false, "deprecationMessage"));
        Mockito.when(config.getPublicConfigOptions()).thenReturn(configOptions);

        String usageString = helpText.getMarkdownForProperties();

        assertTrue(usageString.contains("Property name | Type | Description | Default value | Deprecation Status | Deprecation Message\n"));
        assertTrue(usageString.contains("-------------\n"));
        assertTrue(usageString.contains("blackduck.url | String | Black Duck URL |  |"));
    }
}

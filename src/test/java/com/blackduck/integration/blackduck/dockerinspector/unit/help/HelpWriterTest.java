package com.blackduck.integration.blackduck.dockerinspector.unit.help;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.help.HelpText;
import com.blackduck.integration.blackduck.dockerinspector.help.HelpWriter;
import com.blackduck.integration.exception.IntegrationException;

@ExtendWith(SpringExtension.class)
public class HelpWriterTest {

    public static final String TEST_PROPERTIES_CONTENT = "test properties content";
    @Mock
    private Config config;

    @Mock
    private HelpText helpText;

    @InjectMocks
    private HelpWriter helpWriter;

    @Test
    public void test() throws IntegrationException, UnsupportedEncodingException, IllegalAccessException {
        Mockito.when(config.getHelpOutputFilePath()).thenReturn("test/output");
        Mockito.when(helpText.getMarkdownForProperties()).thenReturn(TEST_PROPERTIES_CONTENT);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
            helpWriter.concatinateContentToPrintStream(ps);
        }

        String helpContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(helpContent.contains(TEST_PROPERTIES_CONTENT));
    }
}

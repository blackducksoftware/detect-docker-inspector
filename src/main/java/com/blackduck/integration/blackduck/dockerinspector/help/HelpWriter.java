/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.help;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.blackduck.dockerinspector.exception.HelpGenerationException;

@Component
public class HelpWriter {
    public static final String PROPERTIES_TOPIC_NAME = "advanced-properties";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HelpText helpText;

    public void writePropertiesMarkdownToDir(File outputDir) throws HelpGenerationException {
        try {
            String markdownFilename = deriveMarkdownFilename(PROPERTIES_TOPIC_NAME);
            try (PrintStream printStreamMarkdown = derivePrintStream(outputDir, markdownFilename)) {
                printStreamMarkdown.println(helpText.getMarkdownForProperties());
            }
        } catch (Exception e) {
            throw new HelpGenerationException(String.format("Error generating help: %s", e.getMessage()), e);
        }
    }

    public void concatinateContentToPrintStream(PrintStream printStream) throws HelpGenerationException {
        try {
            printStream.println(helpText.getMarkdownForProperties());
        } catch (Exception e) {
            throw new HelpGenerationException(String.format("Error generating help: %s", e.getMessage()), e);
        }
    }

    private String deriveMarkdownFilename(String helpTopicName) {
        return String.format("%s.md", helpTopicName);
    }

    private PrintStream derivePrintStream(File outputDir, String markdownFilename) throws FileNotFoundException {
        File finalHelpOutputFile = new File(outputDir, markdownFilename);
        logger.info(String.format("Writing help output to: %s", finalHelpOutputFile.getAbsolutePath()));
        PrintStream printStream = new PrintStream(finalHelpOutputFile);
        return printStream;
    }
}

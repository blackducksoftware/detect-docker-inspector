/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.dockerinspector.help;

import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.dockerinspector.config.Config;
import com.synopsys.integration.blackduck.dockerinspector.config.DockerInspectorOption;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class HelpText {

    @Autowired
    private Config config;

    @Autowired
    private HelpTopicParser helpTopicParser;

    @Autowired
    private HelpReader helpReader;

    public String getMarkdownForTopics(String givenHelpTopicNames) throws IntegrationException, IllegalAccessException {
        String helpTopicNames = helpTopicParser.translateGivenTopicNames(givenHelpTopicNames);
        String markdownContent = collectMarkdownContent(helpTopicNames);
        return markdownContent;
    }

    public String getMarkdownForTopic(String helpTopicName) throws IntegrationException, IllegalAccessException {
        if (helpTopicParser.HELP_TOPIC_NAME_PROPERTIES.equalsIgnoreCase(helpTopicName)) {
            return getMarkdownForProperties();
        } else {
            return helpReader.getVariableSubstitutedTextFromHelpFile(helpTopicName);
        }
    }

    private String collectMarkdownContent(String helpTopicNames) throws IntegrationException, IllegalAccessException {
        List<String> helpTopics = helpTopicParser.deriveHelpTopicList(helpTopicNames);
        StringBuilder markdownContent = new StringBuilder();
        for (String helpTopicName : helpTopics) {
            markdownContent.append(getMarkdownForTopic(helpTopicName));
            markdownContent.append("\n");
        }
        return markdownContent.toString();
    }

    private String getMarkdownForProperties() throws IllegalAccessException, IntegrationException {
        StringBuilder usage = new StringBuilder();
        usage.append("The table below lists advanced Docker Inspector properties that can be set using the \"detect.docker.passthrough.\" property prefix:\n\n");
        usage.append("Property name | Type | Description | Default value | Deprecation Status | Deprecation Message\n");
        usage.append("------------- | ---- | ----------- | ------------- | ------------------ | -------------------\n");
        SortedSet<DockerInspectorOption> configOptions = config.getPublicConfigOptions();
        for (DockerInspectorOption opt : configOptions) {
            StringBuilder usageLine = new StringBuilder(String.format("%s | %s | %s | ", opt.getKey(), opt.getValueTypeString(), opt.getDescription()));
            if (!StringUtils.isBlank(opt.getDefaultValue())) {
                usageLine.append(opt.getDefaultValue());
            } else {
                usageLine.append(" ");
            }
            usageLine.append("| ");
            if (opt.isDeprecated()) {
                usageLine.append("Deprecated");
                usageLine.append(" | ");
                if (StringUtils.isNotBlank(opt.getDeprecationMessage())) {
                    usageLine.append(opt.getDeprecationMessage());
                }
            } else {
                usageLine.append("  |  ");
            }
            usageLine.append("| ");
            usage.append(usageLine.toString());
            usage.append("\n");
        }
        return usage.toString();
    }
}

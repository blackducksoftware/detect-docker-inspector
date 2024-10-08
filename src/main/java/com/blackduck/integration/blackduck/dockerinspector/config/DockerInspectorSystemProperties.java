/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.exception.IntegrationException;

@Component
public class DockerInspectorSystemProperties {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void augmentSystemProperties(String additionalSystemPropertiesPath) throws IntegrationException {
        if (StringUtils.isNotBlank(additionalSystemPropertiesPath)) {
            logger.debug(String.format("Reading user-provided additional System properties: %s", additionalSystemPropertiesPath));
            File additionalSystemPropertiesFile = new File(additionalSystemPropertiesPath);
            Properties additionalSystemProperties = new Properties();
            try (InputStream additionalSystemPropertiesInputStream = new FileInputStream(additionalSystemPropertiesFile)) {
                additionalSystemProperties.load(additionalSystemPropertiesInputStream);
                for (Object key : additionalSystemProperties.keySet()) {
                    String keyString = (String) key;
                    String value = additionalSystemProperties.getProperty(keyString);
                    logger.trace(String.format("additional system property: %s", keyString));
                    System.setProperty(keyString, value);
                }
            } catch (IOException e) {
                String msg = String.format("Error loading additional system properties from %s: %s", additionalSystemPropertiesPath, e.getMessage());
                throw new IntegrationException(msg);
            }
        }
    }
}

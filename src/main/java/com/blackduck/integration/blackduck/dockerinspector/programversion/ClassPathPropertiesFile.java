/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.programversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClassPathPropertiesFile {
    private final Properties props;

    public ClassPathPropertiesFile(String propetiesFilename) throws IOException {
        props = new Properties();
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(propetiesFilename)) {
            props.load(stream);
        }
    }

    public Properties getProperties() {
        return props;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
}

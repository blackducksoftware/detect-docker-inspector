/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.output;

public class BdioFilename {
    private final String spdxName;

    public BdioFilename(final String spdxName) {
        this.spdxName = spdxName;
    }

    public String getBdioFilename() {
        final String bdioFilename = String.format("%s_bdio.jsonld", spdxName);
        return bdioFilename;
    }
}

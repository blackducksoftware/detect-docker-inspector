/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.httpclient;

import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;

public class InspectorImage {
    private final ImageInspectorOsEnum os;
    private final String imageName;
    private final String imageVersion;

    public InspectorImage(final ImageInspectorOsEnum os, final String imageName, final String imageVersion) {
        this.os = os;
        this.imageName = imageName;
        this.imageVersion = imageVersion;
    }

    ImageInspectorOsEnum getOs() {
        return os;
    }

    String getImageName() {
        return imageName;
    }

    String getImageVersion() {
        return imageVersion;
    }

}

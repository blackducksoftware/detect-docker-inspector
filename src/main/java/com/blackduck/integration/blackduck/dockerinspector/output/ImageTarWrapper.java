/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.output;

import java.io.File;

public class ImageTarWrapper {

    private final File file;
    private final String imageRepo;
    private final String imageTag;

    public ImageTarWrapper(final File file, final String imageRepo, final String imageTag) {
        this.file = file;
        this.imageRepo = imageRepo;
        this.imageTag = imageTag;
    }

    public ImageTarWrapper(final File file) {
        this.file = file;
        this.imageRepo = null;
        this.imageTag = null;
    }

    public File getFile() {
        return file;
    }

    public String getImageRepo() {
        return imageRepo;
    }

    public String getImageTag() {
        return imageTag;
    }
}

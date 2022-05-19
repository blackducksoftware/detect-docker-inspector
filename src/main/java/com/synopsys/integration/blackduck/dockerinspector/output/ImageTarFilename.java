/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.dockerinspector.output;

import org.springframework.stereotype.Component;

@Component
public class ImageTarFilename {

    public String deriveImageTarFilenameFromImageTag(String imageName, String tagName) {
        return String.format("%s_%s.tar", cleanImageName(imageName), tagName);
    }

    private String cleanImageName(String imageName) {
        return colonsToUnderscores(slashesToUnderscore(imageName));
    }

    private String colonsToUnderscores(String imageName) {
        return imageName.replace(":", "_");
    }

    private String slashesToUnderscore(String givenString) {
        return givenString.replace("/", "_");
    }
}

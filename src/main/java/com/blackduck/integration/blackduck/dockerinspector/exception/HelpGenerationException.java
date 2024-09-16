/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.exception;

import com.synopsys.integration.exception.IntegrationException;

public class HelpGenerationException extends IntegrationException {

    public HelpGenerationException(final String message) {
        super(message);
    }

    public HelpGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

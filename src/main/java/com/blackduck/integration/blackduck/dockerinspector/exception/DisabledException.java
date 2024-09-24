/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.exception;

import com.blackduck.integration.exception.IntegrationException;

public class DisabledException extends IntegrationException {
    private static final long serialVersionUID = -8752417293450489927L;

    public DisabledException(final String message) {
        super(message);
    }

    public DisabledException(final String message, final Throwable cause) {
        super(message, cause);
    }

}

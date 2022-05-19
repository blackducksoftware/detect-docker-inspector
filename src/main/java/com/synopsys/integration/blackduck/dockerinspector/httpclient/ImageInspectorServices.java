/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.dockerinspector.httpclient;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.dockerinspector.config.Config;
import com.synopsys.integration.blackduck.imageinspector.api.ImageInspectorOsEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;

@Component
public class ImageInspectorServices {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int MAX_CONTAINER_START_TRY_COUNT = 30;

    @Autowired
    private Config config;

    @Autowired
    private HttpRequestor httpRequestor;

    public int getImageInspectorHostPort(ImageInspectorOsEnum imageInspectorOs) throws IntegrationException {
        if (ImageInspectorOsEnum.ALPINE.equals(imageInspectorOs)) {
            return config.getImageInspectorHostPortAlpine();
        }
        if (ImageInspectorOsEnum.CENTOS.equals(imageInspectorOs)) {
            return config.getImageInspectorHostPortCentos();
        }
        if (ImageInspectorOsEnum.UBUNTU.equals(imageInspectorOs)) {
            return config.getImageInspectorHostPortUbuntu();
        }
        throw new IntegrationException(String.format("Unrecognized ImageInspector OS: %s", imageInspectorOs.toString()));
    }

    public int getImageInspectorContainerPort(ImageInspectorOsEnum imageInspectorOs) throws IntegrationException {
        if (ImageInspectorOsEnum.ALPINE.equals(imageInspectorOs)) {
            return config.getImageInspectorContainerPortAlpine();
        }
        if (ImageInspectorOsEnum.CENTOS.equals(imageInspectorOs)) {
            return config.getImageInspectorContainerPortCentos();
        }
        if (ImageInspectorOsEnum.UBUNTU.equals(imageInspectorOs)) {
            return config.getImageInspectorContainerPortUbuntu();
        }
        throw new IntegrationException(String.format("Unrecognized ImageInspector OS: %s", imageInspectorOs));
    }

    public int getDefaultImageInspectorHostPortBasedOnDistro() throws IntegrationException {
        String inspectorOsName = config.getImageInspectorDefaultDistro();
        if ("alpine".equalsIgnoreCase(inspectorOsName)) {
            return config.getImageInspectorHostPortAlpine();
        }
        if ("centos".equalsIgnoreCase(inspectorOsName)) {
            return config.getImageInspectorHostPortCentos();
        }
        if ("ubuntu".equalsIgnoreCase(inspectorOsName)) {
            return config.getImageInspectorHostPortUbuntu();
        }
        throw new IntegrationException(String.format("Invalid value for property image.inspector.default: %s", inspectorOsName));
    }

    public boolean startService(IntHttpClient httpClient, URI imageInspectorUri, String imageInspectorRepo, String imageInspectorTag) throws InterruptedException {
        boolean serviceIsUp = false;
        long timeoutMilliseconds = config.getServiceTimeout() / MAX_CONTAINER_START_TRY_COUNT;
        for (int tryIndex = 0; tryIndex < MAX_CONTAINER_START_TRY_COUNT && !serviceIsUp; tryIndex++) {
            logger.debug(String.format("Pausing %d seconds to give service time to start up", (int) (timeoutMilliseconds / 1000L)));
            Thread.sleep(timeoutMilliseconds);
            logger.debug(String.format("Checking service %s to see if it is up; attempt %d of %d", imageInspectorUri.toString(), tryIndex + 1, MAX_CONTAINER_START_TRY_COUNT));
            serviceIsUp = checkServiceHealth(httpClient, imageInspectorUri);
        }
        return serviceIsUp;
    }

    public boolean checkServiceHealth(IntHttpClient httpClient, URI imageInspectorUri) {
        logger.debug(String.format("Sending request for health check to: %s", imageInspectorUri));
        String healthCheckResponse;
        try {
            healthCheckResponse = httpRequestor
                .executeSimpleGetRequest(httpClient, imageInspectorUri, "health");
        } catch (IntegrationException e) {
            logger.debug(String.format("Health check failed: %s", e.getMessage()));
            return false;
        }
        logger.debug(String.format("ImageInspector health check response: %s", healthCheckResponse));
        boolean serviceIsUp = healthCheckResponse.contains("\"status\":\"UP\"");
        return serviceIsUp;
    }

    public String getServiceVersion(IntHttpClient httpClient, URI imageInspectorUri) {
        logger.debug(String.format("Sending request for service version to: %s", imageInspectorUri));
        String serviceVersionResponse;
        try {
            serviceVersionResponse = httpRequestor
                .executeSimpleGetRequest(httpClient, imageInspectorUri, "getversion");
        } catch (IntegrationException e) {
            logger.debug(String.format("Get ImageInspector service version request failed: %s", e.getMessage()));
            return "unknown";
        }
        logger.debug(String.format("ImageInspector service version response: %s", serviceVersionResponse));
        return serviceVersionResponse;
    }
}

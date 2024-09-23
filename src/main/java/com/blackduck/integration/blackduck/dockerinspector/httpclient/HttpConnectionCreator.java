/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.httpclient;

import java.net.MalformedURLException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.blackduck.integration.blackduck.dockerinspector.httpclient.connection.NonRedirectingIntHttpClient;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;

@Component
public class HttpConnectionCreator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Be sure to close the rest connection
    public IntHttpClient createNonRedirectingConnection(URI baseUri, int timeoutSeconds) throws MalformedURLException {
        logger.debug(String.format("Creating a rest connection (%d second timeout) for URL: %s", timeoutSeconds, baseUri.toString()));
        IntLogger intLogger = new Slf4jIntLogger(logger);
        IntHttpClient connection = new NonRedirectingIntHttpClient(intLogger, new Gson(), timeoutSeconds, false, ProxyInfo.NO_PROXY_INFO);
        return connection;

    }

    // Be sure to close the rest connection
    public IntHttpClient createRedirectingConnection(URI baseUri, int timeoutSeconds) throws MalformedURLException {
        logger.debug(String.format("Creating a rest connection (%d second timeout) for URL: %s", timeoutSeconds, baseUri.toString()));
        IntLogger intLogger = new Slf4jIntLogger(logger);
        IntHttpClient connection = new IntHttpClient(intLogger, new Gson(), timeoutSeconds, false, ProxyInfo.NO_PROXY_INFO);
        return connection;
    }
}

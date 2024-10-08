/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.exception.IntegrationException;

public class ImageInspectorUrlBuilder {
    private static final String INITIAL_PARAMETER_FORMAT_STRING = "%s=%s";
    private static final String SUBSEQUENT_PARAMETER_FORMAT_STRING = "&%s=%s";
    private static final String SUBSEQUENT_PARAMETER_FORMAT_STRING_BINARY = "&%s=%b";
    private static final String GETBDIO_ENDPOINT = "getbdio";
    private static final String LOGGING_LEVEL_QUERY_PARAM = "logginglevel";
    private static final String ORGANIZE_COMPONENTS_BY_LAYER_QUERY_PARAM = "organizecomponentsbylayer";
    private static final String INCLUDE_REMOVED_COMPONENTS_QUERY_PARAM = "includeremovedcomponents";
    private static final String CLEANUP_QUERY_PARAM = "cleanup";
    private static final String RESULTING_CONTAINER_FS_PATH_QUERY_PARAM = "resultingcontainerfspath";
    private static final String CONTAINER_FILESYSTEM_EXCLUDED_PATHS_PARAM = "resultingcontainerfsexcludedpaths";
    private static final String IMAGE_REPO_QUERY_PARAM = "imagerepo";
    private static final String IMAGE_TAG_QUERY_PARAM = "imagetag";
    private static final String TARFILE_QUERY_PARAM = "tarfile";
    private static final String PLATFORM_TOP_LAYER_ID_PARAM = "platformtoplayerid";
    private static final String TARGET_LINUX_DISTRO_PARAM = "targetlinuxdistro";
    private static final String BASE_LOGGER_NAME = "com.blackduck";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private URI imageInspectorUri = null;
    private String containerPathToTarfile = null;
    private String givenImageRepo = null;
    private String givenImageTag = null;
    private String platformTopLayerId = null;
    private String targetLinuxDistro = null;
    private String containerPathToContainerFileSystemFile = null;
    private String containerFileSystemExcludedPaths = null;
    private boolean organizeComponentsByLayer = false;
    private boolean includeRemovedComponents = false;
    private boolean cleanup = true;

    public ImageInspectorUrlBuilder imageInspectorUri(final URI imageInspectorUri) {
        this.imageInspectorUri = imageInspectorUri;
        return this;
    }

    public ImageInspectorUrlBuilder containerPathToTarfile(final String containerPathToTarfile) {
        this.containerPathToTarfile = containerPathToTarfile;
        return this;
    }

    public ImageInspectorUrlBuilder givenImageRepo(final String givenImageRepo) {
        this.givenImageRepo = givenImageRepo;
        return this;
    }

    public ImageInspectorUrlBuilder givenImageTag(final String givenImageTag) {
        this.givenImageTag = givenImageTag;
        return this;
    }

    public ImageInspectorUrlBuilder platformTopLayerId(final String platformTopLayerId) {
        this.platformTopLayerId = platformTopLayerId;
        return this;
    }

    public ImageInspectorUrlBuilder targetLinuxDistro(final String targetLinuxDistro) {
        this.targetLinuxDistro = targetLinuxDistro;
        return this;
    }

    public ImageInspectorUrlBuilder containerPathToContainerFileSystemFile(final String containerPathToContainerFileSystemFile) {
        this.containerPathToContainerFileSystemFile = containerPathToContainerFileSystemFile;
        return this;
    }

    public ImageInspectorUrlBuilder containerFileSystemExcludedPaths(final String containerFileSystemExcludedPaths) {
        this.containerFileSystemExcludedPaths = containerFileSystemExcludedPaths;
        return this;
    }

    public ImageInspectorUrlBuilder organizeComponentsByLayer(final boolean organizeComponentsByLayer) {
        this.organizeComponentsByLayer = organizeComponentsByLayer;
        return this;
    }

    public ImageInspectorUrlBuilder includeRemovedComponents(final boolean includeRemovedComponents) {
        this.includeRemovedComponents = includeRemovedComponents;
        return this;
    }

    public ImageInspectorUrlBuilder cleanup(final boolean cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    public String build() throws IntegrationException {
        if (imageInspectorUri == null) {
            throw new IntegrationException("imageInspectorUri not specified");
        }
        if (containerPathToTarfile == null) {
            throw new IntegrationException("imageInspectorUri not specified");
        }
        final StringBuilder urlSb = new StringBuilder();
        urlSb.append(imageInspectorUri.toString());
        urlSb.append("/");
        urlSb.append(GETBDIO_ENDPOINT);
        urlSb.append("?");
        urlSb.append(String.format(INITIAL_PARAMETER_FORMAT_STRING, LOGGING_LEVEL_QUERY_PARAM, getLoggingLevel()));
        urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, TARFILE_QUERY_PARAM, urlEncode(containerPathToTarfile)));
        urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING_BINARY, ORGANIZE_COMPONENTS_BY_LAYER_QUERY_PARAM, organizeComponentsByLayer));
        urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING_BINARY, INCLUDE_REMOVED_COMPONENTS_QUERY_PARAM, includeRemovedComponents));
        urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING_BINARY, CLEANUP_QUERY_PARAM, cleanup));
        if (StringUtils.isNotBlank(containerPathToContainerFileSystemFile)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, RESULTING_CONTAINER_FS_PATH_QUERY_PARAM, urlEncode(containerPathToContainerFileSystemFile)));
        }
        if (StringUtils.isNotBlank(containerFileSystemExcludedPaths)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, CONTAINER_FILESYSTEM_EXCLUDED_PATHS_PARAM, urlEncode(containerFileSystemExcludedPaths)));
        }
        if (StringUtils.isNotBlank(givenImageRepo)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, IMAGE_REPO_QUERY_PARAM, givenImageRepo));
        }
        if (StringUtils.isNotBlank(givenImageTag)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, IMAGE_TAG_QUERY_PARAM, givenImageTag));
        }
        if (StringUtils.isNotBlank(platformTopLayerId)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, PLATFORM_TOP_LAYER_ID_PARAM, platformTopLayerId));
        }
        if (StringUtils.isNotBlank(targetLinuxDistro)) {
            urlSb.append(String.format(SUBSEQUENT_PARAMETER_FORMAT_STRING, TARGET_LINUX_DISTRO_PARAM, targetLinuxDistro));
        }
        final String url = urlSb.toString();
        return url;
    }

    private String urlEncode(final String s) throws IntegrationException {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IntegrationException(String.format("Error URL encoding: %s", s));
        }
    }

    private String getLoggingLevel() {
        String loggingLevel = "INFO";
        try {
            final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(BASE_LOGGER_NAME);
            loggingLevel = root.getLevel().toString();
            logger.debug(String.format("Logging level: %s", loggingLevel));
        } catch (final Exception e) {
            logger.debug(String.format("No logging level set. Defaulting to %s", loggingLevel));
        }
        return loggingLevel;
    }
}

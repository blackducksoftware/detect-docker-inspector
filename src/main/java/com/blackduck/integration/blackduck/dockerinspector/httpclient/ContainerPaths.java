/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.httpclient;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;

@Component
public class ContainerPaths {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Config config;
    private ProgramPaths programPaths;

    @Autowired
    public ContainerPaths(Config config, ProgramPaths programPaths) {
        this.config = config;
        this.programPaths = programPaths;
    }

    /*
     * Translate a local path (to a file within the dir shared with the container) to the equivalent path for the container. Find path to the given localPath RELATIVE to the local shared dir. Convert that to the container's path by
     * appending that relative path to the container's path to the shared dir
     */
    public String getContainerPathToTargetFile(String localPathToTargetFile) throws IOException {
        localPathToTargetFile = toLinux(localPathToTargetFile);
        logger.debug(String.format("localPathToTargetFile: %s", localPathToTargetFile));
        final String sharedDirPathLocal = toLinux(new File(config.getSharedDirPathLocal()).getCanonicalPath());
        logger.debug(String.format("sharedDirPathLocal: %s", sharedDirPathLocal));
        final String sharedDirPathImageInspector = toLinux(config.getSharedDirPathImageInspector());
        logger.debug(String.format("sharedDirPathImageInspector: %s", sharedDirPathImageInspector));
        final String localRelPath = localPathToTargetFile.substring(sharedDirPathLocal.length());
        logger.debug(String.format("localRelPath: %s", localRelPath));
        final File containerFile = getFileInDir(sharedDirPathImageInspector, localRelPath);
        String containerFilePath = toLinux(containerFile.getCanonicalPath());
        logger.debug(String.format("containerPath: %s", containerFilePath));
        return containerFilePath;
    }

    public String getContainerPathToOutputFile(final String outputFilename) throws IOException {
        final File containerFileSystemFileInContainer = new File(getContainerOutputDir(), outputFilename);
        String containerFileSystemFileInContainerPath = toLinux(containerFileSystemFileInContainer.getCanonicalPath());
        logger.debug(String.format("Image inspector container's path to the target container file system file: %s", containerFileSystemFileInContainerPath));
        return containerFileSystemFileInContainerPath;
    }

    private String toLinux(final String givenPath) {
        String drivelessPath = File.separator + FilenameUtils.separatorsToSystem(givenPath).substring(FilenameUtils.getPrefixLength(givenPath));
        return drivelessPath.replace('\\', '/');
    }

    private File getContainerOutputDir() {
        final File containerRunDir = getFileInDir(config.getSharedDirPathImageInspector(), programPaths.getDockerInspectorRunDirName());
        final File containerOutputDir = new File(containerRunDir, ProgramPaths.OUTPUT_DIR);
        return containerOutputDir;
    }

    private File getFileInDir(final String dirPath, final String filename) {
        final File containerOutputDir = new File(dirPath);
        final File containerFileSystemFileInContainer = new File(containerOutputDir, filename);
        return containerFileSystemFileInContainer;
    }
}

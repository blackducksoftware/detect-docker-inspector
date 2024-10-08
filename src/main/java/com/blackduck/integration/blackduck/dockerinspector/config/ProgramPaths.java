/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.blackduck.dockerinspector.ProcessId;

@Component
public class ProgramPaths {
    private final Config config;
    public static final String RESULTS_JSON_FILENAME = "results.json";
    private static final String RUNDIR_BASENAME = "run";
    public static final String OUTPUT_DIR = "output";
    private static final String TARGET_DIR = "target";
    private static final String CONFIG_DIR = "config";
    private static final String SQUASHED_IMAGE_DIR = "squashedImageBuildDir";
    private static final String SQUASHED_IMAGE_TARFILE_DIR = "squashedImageTarDir";
    private static final String SQUASHED_IMAGE_TARFILE_NAME = "squashedImage.tar";
    private final String dockerInspectorPgmDirPath;
    private final String dockerInspectorRunDirName;
    private final String dockerInspectorRunDirPath;

    public static final String APPLICATION_PROPERTIES_FILENAME = "application.properties";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String dockerInspectorConfigDirPath;
    private final String dockerInspectorConfigFilePath;
    private final String dockerInspectorTargetDirPath;
    private final String dockerInspectorSquashedImageDirPath;
    private final String dockerInspectorSquashedImageTarFilePath;
    private final String dockerInspectorWorkingOutputPath;

    @Autowired
    public ProgramPaths(Config config, ProcessId processId) throws IOException {
        this.config = config;

        dockerInspectorPgmDirPath = getProgramDirPath();
        logger.debug(String.format("dockerInspectorPgmDirPath: %s", dockerInspectorPgmDirPath));
        dockerInspectorRunDirName = processId.addProcessIdToName(RUNDIR_BASENAME);
        File runDir = new File(dockerInspectorPgmDirPath, dockerInspectorRunDirName);
        dockerInspectorRunDirPath = runDir.getCanonicalPath() + File.separator;
        logger.debug(String.format("dockerInspectorRunDirPath: %s", dockerInspectorRunDirPath));
        dockerInspectorConfigDirPath = new File(runDir, CONFIG_DIR).getCanonicalPath() + File.separator;
        dockerInspectorConfigFilePath = dockerInspectorConfigDirPath + APPLICATION_PROPERTIES_FILENAME;
        dockerInspectorTargetDirPath = new File(runDir, TARGET_DIR).getCanonicalPath() + File.separator;
        dockerInspectorSquashedImageDirPath = new File(runDir, SQUASHED_IMAGE_DIR).getCanonicalPath() + File.separator;
        File dockerInspectorSquashedImageTarFileDir = new File(runDir, SQUASHED_IMAGE_TARFILE_DIR);
        dockerInspectorSquashedImageTarFilePath = new File(dockerInspectorSquashedImageTarFileDir, SQUASHED_IMAGE_TARFILE_NAME).getCanonicalPath();
        dockerInspectorWorkingOutputPath = new File(runDir, OUTPUT_DIR).getCanonicalPath() + File.separator;
    }

    private String getProgramDirPath() throws IOException {
        File workingDir = new File(config.getWorkingDirPath());
        logger.debug(String.format("getProgramDirPath(): returning %s", config.getWorkingDirPath()));
        return workingDir.getAbsolutePath();
    }

    public String getUserOutputDirPath() {
        if (StringUtils.isBlank(config.getOutputPath())) {
            return null;
        }
        return config.getOutputPath();
    }

    public String getDockerInspectorConfigDirPath() {
        return dockerInspectorConfigDirPath;
    }

    public String getDockerInspectorConfigFilePath() {
        return dockerInspectorConfigFilePath;
    }

    public String getDockerInspectorTargetDirPath() {
        return dockerInspectorTargetDirPath;
    }

    public String getDockerInspectorSquashedImageDirPath() {
        return dockerInspectorSquashedImageDirPath;
    }

    public String getDockerInspectorSquashedImageTarFilePath() { return dockerInspectorSquashedImageTarFilePath; }

    public String getDockerInspectorPgmDirPath() {
        return dockerInspectorPgmDirPath;
    }

    public String getDockerInspectorRunDirName() {
        return dockerInspectorRunDirName;
    }

    public String getDockerInspectorRunDirPath() {
        return dockerInspectorRunDirPath;
    }

    public String getDockerInspectorWorkingOutputPath() {
        return dockerInspectorWorkingOutputPath;
    }

    public String getDockerInspectorResultsFilename() {
        return RESULTS_JSON_FILENAME;
    }
}

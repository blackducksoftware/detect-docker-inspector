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
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.blackduck.integration.bdio.BdioReader;
import com.blackduck.integration.bdio.model.SimpleBdioDocument;
import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;
import com.blackduck.integration.blackduck.dockerinspector.dockerclient.DockerClientManager;
import com.blackduck.integration.blackduck.dockerinspector.output.ContainerFilesystemFilename;
import com.blackduck.integration.blackduck.dockerinspector.output.ImageTarWrapper;
import com.blackduck.integration.blackduck.dockerinspector.output.Output;
import com.blackduck.integration.blackduck.dockerinspector.output.OutputFiles;
import com.blackduck.integration.blackduck.dockerinspector.output.Result;
import com.blackduck.integration.blackduck.imageinspector.linux.FileOperations;
import com.blackduck.integration.exception.IntegrationException;

@Component
public class HttpClientInspector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FileOperations fileOperations;

    @Autowired
    private Config config;

    @Autowired
    private ProgramPaths programPaths;

    @Autowired
    private DockerClientManager dockerClientManager;

    @Autowired
    private List<ImageInspectorClient> imageInspectorClients;

    @Autowired
    private ContainerPaths containerPaths;

    @Autowired
    private Output output;

    @Autowired
    private Gson gson;

    @Autowired
    private ContainerFilesystemFilename containerFilesystemFilename;

    public Result getBdio() throws IntegrationException, InterruptedException {
        ImageInspectorClient imageInspectorClient = chooseImageInspectorClient();
        try {
            output.ensureWorkingOutputDirIsWriteable();
            ImageTarWrapper finalDockerTarfile = prepareDockerTarfile(imageInspectorClient);
            String containerFileSystemFilename = containerFilesystemFilename.deriveContainerFilesystemFilename(finalDockerTarfile.getImageRepo(), finalDockerTarfile.getImageTag());
            String dockerTarFilePathInContainer = containerPaths.getContainerPathToTargetFile(finalDockerTarfile.getFile().getCanonicalPath());
            String containerFileSystemPathInContainer = null;
            if (config.isOutputIncludeContainerfilesystem() || config.isOutputIncludeSquashedImage()) {
                containerFileSystemPathInContainer = containerPaths.getContainerPathToOutputFile(containerFileSystemFilename);
            }
            String bdioString = imageInspectorClient.getBdio(finalDockerTarfile.getFile().getCanonicalPath(), dockerTarFilePathInContainer, config.getDockerImageRepo(), config.getDockerImageTag(),
                containerFileSystemPathInContainer, config.getContainerFileSystemExcludedPaths(),
                config.isOrganizeComponentsByLayer(), config.isIncludeRemovedComponents(),
                config.isCleanupWorkingDir(), config.getDockerPlatformTopLayerId(),
                config.getTargetImageLinuxDistroOverride()
            );
            logger.trace(String.format("bdioString: %s", bdioString));
            SimpleBdioDocument bdioDocument = toBdioDocument(bdioString);
            adjustBdio(bdioDocument);
            OutputFiles outputFiles = output.addOutputToFinalOutputDir(bdioDocument, finalDockerTarfile.getImageRepo(), finalDockerTarfile.getImageTag());
            cleanup();
            Result result = Result.createResultSuccess(finalDockerTarfile.getImageRepo(), finalDockerTarfile.getImageTag(), finalDockerTarfile.getFile().getName(),
                outputFiles.getBdioFile(),
                outputFiles.getContainerFileSystemFile(),
                outputFiles.getSquashedImageFile()
            );
            return result;
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void adjustBdio(SimpleBdioDocument bdioDocument) {
        if (StringUtils.isNotBlank(config.getBdioProjectName())) {
            bdioDocument.getProject().name = config.getBdioProjectName();
        }
        if (StringUtils.isNotBlank(config.getBdioProjectVersion())) {
            bdioDocument.getProject().version = config.getBdioProjectVersion();
        }
        if (StringUtils.isNotBlank(config.getBdioCodelocationName())) {
            bdioDocument.getBillOfMaterials().spdxName = config.getBdioCodelocationName();
        } else if (StringUtils.isNotBlank(config.getBdioCodelocationPrefix())) {
            bdioDocument.getBillOfMaterials().spdxName = String.format("%s_%s", config.getBdioCodelocationPrefix(), bdioDocument.getBillOfMaterials().spdxName);
        }
    }

    private SimpleBdioDocument toBdioDocument(String bdioString) throws IOException {
        Reader reader = new StringReader(bdioString);
        SimpleBdioDocument doc = null;
        try (BdioReader bdioReader = new BdioReader(gson, reader)) {
            doc = bdioReader.readSimpleBdioDocument();
            return doc;
        }
    }

    private ImageTarWrapper prepareDockerTarfile(ImageInspectorClient imageInspectorClient) throws IOException, IntegrationException {
        ImageTarWrapper givenDockerTarfile = dockerClientManager.deriveDockerTarFileFromConfig();
        ImageTarWrapper finalDockerTarfile = imageInspectorClient.copyTarfileToSharedDir(fileOperations, config, programPaths, givenDockerTarfile);
        return finalDockerTarfile;
    }

    private void cleanup() {
        if (!config.isCleanupWorkingDir()) {
            return;
        }
        logger.debug(String.format("Removing %s", programPaths.getDockerInspectorRunDirPath()));
        try {
            removeFileOrDir(programPaths.getDockerInspectorRunDirPath());
        } catch (IOException e) {
            logger.error(String.format("Error cleaning up working directories: %s", e.getMessage()));
        }
    }

    private void removeFileOrDir(String fileOrDirPath) throws IOException {
        logger.info(String.format("Removing file or dir: %s", fileOrDirPath));
        File fileOrDir = new File(fileOrDirPath);
        if (fileOrDir.exists()) {
            if (fileOrDir.isDirectory()) {
                FileUtils.deleteDirectory(fileOrDir);
            } else {
                FileUtils.deleteQuietly(fileOrDir);
            }
        }
    }

    private ImageInspectorClient chooseImageInspectorClient() throws IntegrationException {
        for (ImageInspectorClient client : imageInspectorClients) {
            if (client.isApplicable()) {
                return client;
            }
        }
        throw new IntegrationException("Invalid configuration: Need to provide URL to existing ImageInspector services, or request that containers be started as-needed");
    }
}

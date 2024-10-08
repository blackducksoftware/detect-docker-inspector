/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.google.gson.Gson;
import com.blackduck.integration.blackduck.dockerinspector.config.Config;
import com.blackduck.integration.blackduck.dockerinspector.config.DockerInspectorSystemProperties;
import com.blackduck.integration.blackduck.dockerinspector.config.ProgramPaths;
import com.blackduck.integration.blackduck.dockerinspector.exception.HelpGenerationException;
import com.blackduck.integration.blackduck.dockerinspector.help.HelpWriter;
import com.blackduck.integration.blackduck.dockerinspector.httpclient.HttpClientInspector;
import com.blackduck.integration.blackduck.dockerinspector.output.Output;
import com.blackduck.integration.blackduck.dockerinspector.output.Result;
import com.blackduck.integration.blackduck.dockerinspector.output.ResultFile;
import com.blackduck.integration.blackduck.dockerinspector.programversion.ProgramVersion;
import com.blackduck.integration.blackduck.imageinspector.api.name.ImageNameResolver;
import com.blackduck.integration.blackduck.imageinspector.image.common.RepoTag;
import com.blackduck.integration.exception.IntegrationException;

@SpringBootApplication
@ComponentScan(basePackages = { "com.blackduck.integration.blackduck.imageinspector", "com.blackduck.integration.blackduck.dockerinspector" })
public class DockerInspector implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DockerInspector.class);

    private static final String DETECT_CALLER_NAME = "Detect";

    @Autowired
    private ProgramVersion programVersion;

    @Autowired
    private ProgramPaths programPaths;

    @Autowired
    private ResultFile resultFile;

    @Autowired
    private ApplicationArguments applicationArguments;

    @Autowired
    private Config config;

    @Autowired
    private HttpClientInspector inspector;

    @Autowired
    private HelpWriter helpWriter;

    @Autowired
    private Output output;

    @Autowired
    private DockerInspectorSystemProperties dockerInspectorSystemProperties;

    public static void main(String[] args) {
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(DockerInspector.class);
        appBuilder.logStartupInfo(false);
        appBuilder.bannerMode(Banner.Mode.OFF);
        appBuilder.run(args);
        logger.warn("The program is not expected to get here.");
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws InterruptedException {
        Result result = null;
        try {
            if (!initAndValidate(config)) {
                System.exit(0);
            }
            result = inspector.getBdio();
        } catch (HelpGenerationException helpGenerationException) {
            String msg = logException(helpGenerationException, "Error generating help: %s");
            result = Result.createResultFailure(msg);
        } catch (InterruptedException ie) {
            logException(ie, "Error inspecting image: %s");
            throw ie;
        } catch (Exception e) {
            String msg = logException(e, "Error inspecting image: %s");
            result = Result.createResultFailure(msg);
        }
        File resultsFile = new File(output.getFinalOutputDir(), programPaths.getDockerInspectorResultsFilename());
        resultFile.write(new Gson(), resultsFile, result);
        int returnCode = result.getReturnCode();
        logger.info(String.format("Returning %d", returnCode));
        System.exit(returnCode);
    }

    private String logException(Exception e, String s) {
        String msg = String.format(s, e.getMessage());
        logger.error(msg);
        logStackTraceIfDebug(e);
        return msg;
    }

    private void logStackTraceIfDebug(Exception e) {
        String trace = ExceptionUtils.getStackTrace(e);
        logger.debug(String.format("Stack trace: %s", trace));
    }

    private boolean helpInvoked() {
        logger.debug("Checking to see if help argument passed");
        if (applicationArguments == null) {
            logger.debug("applicationArguments is null");
            return false;
        }
        String[] args = applicationArguments.getSourceArgs();
        if (contains(args, "-h") || contains(args, "--help") || contains(args, "--help=true")) {
            logger.debug("Help argument passed");
            return true;
        }
        return false;
    }

    private boolean contains(String[] stringsToSearch, String targetString) {
        for (String stringToTest : stringsToSearch) {
            if (targetString.equals(stringToTest)) {
                return true;
            }
        }
        return false;
    }

    private boolean initAndValidate(Config config) throws IntegrationException, FileNotFoundException {
        logger.info(String.format("Detect Docker Inspector %s", programVersion.getProgramVersion()));
        logger.debug(String.format("Java version: %s", System.getProperty("java.version")));
        if (helpInvoked()) {
            provideHelp(config);
            return false;
        }
        if (!DETECT_CALLER_NAME.equals(config.getCallerName())) {
            throw new IntegrationException("Running Docker Inspector as a standalone utility is no longer supported. You must invoke Docker Inspector by running Detect.");
        }
        dockerInspectorSystemProperties.augmentSystemProperties(config.getSystemPropertiesPath());
        logger.debug(String.format("running from dir: %s", System.getProperty("user.dir")));
        logger.trace(String.format("dockerImageTag: %s", config.getDockerImageTag()));
        initImageName();
        logger.info(String.format("Inspecting image:tag %s:%s (platform: %s)",
            config.getDockerImageRepo(), config.getDockerImageTag(),
            Optional.ofNullable(config.getDockerImagePlatform()).orElse("<unspecified>")
        ));
        return true;
    }

    private void provideHelp(Config config) throws FileNotFoundException, HelpGenerationException {
        String givenHelpOutputFilePath = config.getHelpOutputFilePath();
        File helpOutputFile = new File(givenHelpOutputFilePath);
        if ((!helpOutputFile.isDirectory())) {
            throw new HelpGenerationException(String.format("%s is not a directory", helpOutputFile.getAbsolutePath()));
        }
        helpWriter.writePropertiesMarkdownToDir(helpOutputFile);
        System.out.println("Finished provideHelp()");
    }

    private void initImageName() {
        logger.debug(String.format("initImageName(): dockerImage: %s, dockerTar: %s", config.getDockerImage(), config.getDockerTar()));
        ImageNameResolver resolver = new ImageNameResolver();
        RepoTag resolvedRepoTag = resolver.resolve(config.getDockerImage(), null, null);
        if (resolvedRepoTag.getRepo().isPresent()) {
            config.setDockerImageRepo(resolvedRepoTag.getRepo().get());
        }
        if (resolvedRepoTag.getTag().isPresent()) {
            config.setDockerImageTag(resolvedRepoTag.getTag().get());
        }
        logger.debug(String.format("initImageName(): final: dockerImage: %s; dockerImageRepo: %s; dockerImageTag: %s", config.getDockerImage(), config.getDockerImageRepo(), config.getDockerImageTag()));
    }
}

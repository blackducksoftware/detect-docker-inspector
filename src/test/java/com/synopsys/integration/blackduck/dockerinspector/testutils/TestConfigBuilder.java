package com.synopsys.integration.blackduck.dockerinspector.testutils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.exception.IntegrationException;

public class TestConfigBuilder {
    private TestConfig.Mode mode = TestConfig.Mode.DEFAULT;
    private String inspectTargetImageRepoTag;
    private String inspectTargetImageId;
    private String tarFilePath;
    private String targetRepo; // tarfile image selector
    private String targetTag;  // tarfile image selector
    private int portOnHost;
    private boolean requireBdioMatch;
    private int minNumberOfComponentsExpected;
    private String outputBomMustContainComponentPrefix;
    private String outputBomMustNotContainComponentPrefix;
    private String outputBomMustContainExternalSystemTypeId;
    private String codelocationName;
    private List<String> additionalArgs;
    private Map<String, String> env;
    private boolean testSquashedImageGeneration;
    private File outputContainerFileSystemFile;
    private File outputSquashedImageFile;
    private File targetTarInSharedDir;
    private long minContainerFileSystemFileSize;
    private long maxContainerFileSystemFileSize;
    private boolean appOnlyMode;
    private String callerName;

    public TestConfigBuilder setAppOnlyMode(boolean appOnlyMode) {
        this.appOnlyMode = appOnlyMode;
        return this;
    }

    public TestConfigBuilder setCallerName(String callerName) {
        this.callerName = callerName;
        return this;
    }

    public TestConfigBuilder setMode(TestConfig.Mode mode) {
        this.mode = mode;
        return this;
    }

    public TestConfigBuilder setInspectTargetImageRepoTag(String inspectTargetImageRepoTag) {
        this.inspectTargetImageRepoTag = inspectTargetImageRepoTag;
        return this;
    }

    public TestConfigBuilder setInspectTargetImageId(String inspectTargetImageId) {
        this.inspectTargetImageId = inspectTargetImageId;
        return this;
    }

    public TestConfigBuilder setTarFilePath(String tarFilePath) {
        this.tarFilePath = tarFilePath;
        return this;
    }

    public TestConfigBuilder setTargetRepo(String targetRepo) {
        this.targetRepo = targetRepo;
        return this;
    }

    public TestConfigBuilder setTargetTag(String targetTag) {
        this.targetTag = targetTag;
        return this;
    }

    public TestConfigBuilder setPortOnHost(int portOnHost) {
        this.portOnHost = portOnHost;
        return this;
    }

    public TestConfigBuilder setRequireBdioMatch(boolean requireBdioMatch) {
        this.requireBdioMatch = requireBdioMatch;
        return this;
    }

    public TestConfigBuilder setMinNumberOfComponentsExpected(int minNumberOfComponentsExpected) {
        this.minNumberOfComponentsExpected = minNumberOfComponentsExpected;
        return this;
    }

    public TestConfigBuilder setOutputBomMustContainComponentPrefix(String outputBomMustContainComponentPrefix) {
        this.outputBomMustContainComponentPrefix = outputBomMustContainComponentPrefix;
        return this;
    }

    public TestConfigBuilder setOutputBomMustNotContainComponentPrefix(String outputBomMustNotContainComponentPrefix) {
        this.outputBomMustNotContainComponentPrefix = outputBomMustNotContainComponentPrefix;
        return this;
    }

    public TestConfigBuilder setOutputBomMustContainExternalSystemTypeId(String outputBomMustContainExternalSystemTypeId) {
        this.outputBomMustContainExternalSystemTypeId = outputBomMustContainExternalSystemTypeId;
        return this;
    }

    public TestConfigBuilder setCodelocationName(String codelocationName) {
        this.codelocationName = codelocationName;
        return this;
    }

    public TestConfigBuilder setAdditionalArgs(List<String> additionalArgs) {
        this.additionalArgs = additionalArgs;
        return this;
    }

    public TestConfigBuilder setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public TestConfigBuilder setTestSquashedImageGeneration(boolean testSquashedImageGeneration) {
        this.testSquashedImageGeneration = testSquashedImageGeneration;
        return this;
    }

    public TestConfigBuilder setOutputContainerFileSystemFile(File outputContainerFileSystemFile) {
        this.outputContainerFileSystemFile = outputContainerFileSystemFile;
        return this;
    }

    public TestConfigBuilder setOutputSquashedImageFile(File outputSquashedImageFile) {
        this.outputSquashedImageFile = outputSquashedImageFile;
        return this;
    }

    public TestConfigBuilder setTargetTarInSharedDir(File targetTarInSharedDir) {
        this.targetTarInSharedDir = targetTarInSharedDir;
        return this;
    }

    public TestConfigBuilder setMinContainerFileSystemFileSize(long minContainerFileSystemFileSize) {
        this.minContainerFileSystemFileSize = minContainerFileSystemFileSize;
        return this;
    }

    public TestConfigBuilder setMaxContainerFileSystemFileSize(long maxContainerFileSystemFileSize) {
        this.maxContainerFileSystemFileSize = maxContainerFileSystemFileSize;
        return this;
    }

    public TestConfig build() throws IntegrationException {
        if ((inspectTargetImageRepoTag == null) && (tarFilePath == null) && (inspectTargetImageId == null)) {
            throw new IntegrationException("Invalid TestConfig");
        }
        return new TestConfig(mode, inspectTargetImageRepoTag, inspectTargetImageId, tarFilePath, targetRepo, targetTag, portOnHost, requireBdioMatch, minNumberOfComponentsExpected,
            outputBomMustContainComponentPrefix, outputBomMustNotContainComponentPrefix,
            outputBomMustContainExternalSystemTypeId, codelocationName, additionalArgs, env, testSquashedImageGeneration,
            outputContainerFileSystemFile, outputSquashedImageFile, targetTarInSharedDir, minContainerFileSystemFileSize, maxContainerFileSystemFileSize,
            appOnlyMode, callerName
        );
    }
}


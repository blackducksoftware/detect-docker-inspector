/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.dockerinspector.help;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.dockerinspector.config.Config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

@Component
public class HelpReader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Config config;

    private Configuration freemarkerConfig = null;

    private void init() throws IOException {
        ensureConfigInitialized();
    }

    private void ensureConfigInitialized() throws IOException {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration(Configuration.VERSION_2_3_29);
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            freemarkerConfig.setLogTemplateExceptions(false);
            freemarkerConfig.setWrapUncheckedExceptions(true);
            freemarkerConfig.setFallbackOnNullLoopVariable(false);

            if (StringUtils.isNotBlank(config.getHelpInputFilePath())) {
                File contentDir = new File(config.getHelpInputFilePath());
                freemarkerConfig.setDirectoryForTemplateLoading(contentDir);
            } else {
                freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/help/content");
            }
        }
    }
}

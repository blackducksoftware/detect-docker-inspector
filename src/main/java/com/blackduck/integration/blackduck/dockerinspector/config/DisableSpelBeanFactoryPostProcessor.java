/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Disables Spring SpEL evaluation for @Value resolution.
 *
 * Spring's default injection pipeline resolves ${property.key} via PropertySourcesPlaceholderConfigurer,
 * then passes the resolved string to AbstractBeanFactory.evaluateBeanDefinitionString(). If the resolved
 * value starts with "#{", StandardBeanExpressionResolver evaluates it as a SpEL expression — even though
 * the @Value annotation itself only contains ${...} syntax.
 *
 * Setting the BeanExpressionResolver to null short-circuits that second step:
 * AbstractBeanFactory.evaluateBeanDefinitionString() returns the value unchanged when the resolver is null.
 * This closes the SpEL injection vector across all 47 @Value fields in Config.java without affecting
 * normal ${property.key} placeholder resolution.
 */
@Configuration
public class DisableSpelBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DisableSpelBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.debug("Disabling SpEL bean expression resolver to prevent @Value SpEL injection (CVE-2026-41849)");
        beanFactory.setBeanExpressionResolver(null);
    }
}


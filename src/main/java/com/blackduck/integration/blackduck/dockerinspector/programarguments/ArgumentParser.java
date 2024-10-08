/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.programarguments;

import java.util.Arrays;

public class ArgumentParser {

    private final String[] args;

    public ArgumentParser(final String[] args) {
        this.args = args;
    }

    public boolean isArgumentPresent(final String command, final String largeCommand) {
        return Arrays.stream(args).anyMatch(arg -> arg.equals(command) || arg.equals(largeCommand));
    }

    public String findValueForCommand(final String command, final String largeCommand) {
        for (int i = 1; i < args.length; i++) {
            final String previousArgument = args[i - 1];
            final String possibleValue = args[i];
            if (command.equals(previousArgument) || largeCommand.equals(previousArgument)) {
                if (isValueAcceptable(possibleValue)) {
                    return possibleValue;
                }
            }
        }
        return null;
    }

    private boolean isValueAcceptable(final String value) {
        if (!value.startsWith("-")) {
            return true;
        }
        return false;
    }

}

package com.blackduck.integration.blackduck.dockerinspector.testutils;

public class ListComparisonData {
    private boolean equalContent;
    private int matchedLines;
    private int ignoredLines;

    public ListComparisonData() {
        equalContent = true;
        matchedLines = 0;
        ignoredLines = 0;
    }

    public ListComparisonData(boolean equalContent, int matchedLines, int ignoredLines) {
        this.equalContent = equalContent;
        this.matchedLines = matchedLines;
        this.ignoredLines = ignoredLines;
    }

    public void matchedLine() {
        matchedLines++;
    }

    public void ignoredLine() {
        ignoredLines++;
    }

    public void equalContent(boolean equalContent) {
        this.equalContent = equalContent;
    }

    public boolean isEqualContent() {
        return equalContent;
    }

    public int getMatchedLines() {
        return matchedLines;
    }

    public int getIgnoredLines() {
        return ignoredLines;
    }
}

package com.mxj.mmitest.domain.model;

/**
 * 测试统计数据
 */
public class TestStatistics {
    private int totalSessions;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private double passRate;
    private TestResultSummary lastSession;

    public TestStatistics() {}

    // Getters and Setters
    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public int getSkippedTests() { return skippedTests; }
    public void setSkippedTests(int skippedTests) { this.skippedTests = skippedTests; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }

    public TestResultSummary getLastSession() { return lastSession; }
    public void setLastSession(TestResultSummary lastSession) { this.lastSession = lastSession; }

    public void calculate() {
        if (totalTests > 0) {
            passRate = (double) passedTests / totalTests * 100;
        } else {
            passRate = 0;
        }
    }
}

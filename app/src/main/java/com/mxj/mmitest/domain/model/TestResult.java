package com.mxj.mmitest.domain.model;

/**
 * 测试结果数据类
 */
public class TestResult {
    private long id;
    private String sessionId;
    private int testItemId;
    private String testItemName;
    private String result; // PASS, FAIL, SKIPPED, TIMEOUT
    private long timestamp;
    private String deviceId;
    private String deviceModel;
    private String deviceManufacturer;
    private String additionalInfo;
    private String timeoutReason;

    public TestResult() {}

    public TestResult(int testItemId, String testItemName, String result,
                      String deviceId, String deviceModel, String deviceManufacturer) {
        this.testItemId = testItemId;
        this.testItemName = testItemName;
        this.result = result;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.deviceManufacturer = deviceManufacturer;
        this.timestamp = System.currentTimeMillis();
        this.additionalInfo = "{}";
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public int getTestItemId() { return testItemId; }
    public void setTestItemId(int testItemId) { this.testItemId = testItemId; }

    public String getTestItemName() { return testItemName; }
    public void setTestItemName(String testItemName) { this.testItemName = testItemName; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getDeviceManufacturer() { return deviceManufacturer; }
    public void setDeviceManufacturer(String deviceManufacturer) { this.deviceManufacturer = deviceManufacturer; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getTimeoutReason() { return timeoutReason; }
    public void setTimeoutReason(String timeoutReason) { this.timeoutReason = timeoutReason; }

    public boolean isPassed() {
        return "PASS".equals(result);
    }
}

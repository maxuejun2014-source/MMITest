package com.mxj.mmitest.ui.autotest;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mxj.mmitest.config.TestConfig;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.domain.model.TestResult;
import com.mxj.mmitest.domain.model.TestResultSummary;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动测试ViewModel
 */
public class AutoTestViewModel extends ViewModel {

    public enum TestState {
        IDLE, RUNNING, PAUSED, COMPLETED, STOPPED
    }

    private TestRepository repository;
    private Handler handler;

    private final MutableLiveData<TestState> testState = new MutableLiveData<>(TestState.IDLE);
    public LiveData<TestState> getTestState() { return testState; }

    private final MutableLiveData<String> currentTestName = new MutableLiveData<>("");
    public LiveData<String> getCurrentTestName() { return currentTestName; }

    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    public LiveData<Integer> getProgress() { return progress; }

    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    public LiveData<Integer> getTotalCount() { return totalCount; }

    private final MutableLiveData<Integer> passedCount = new MutableLiveData<>(0);
    public LiveData<Integer> getPassedCount() { return passedCount; }

    private final MutableLiveData<Integer> failedCount = new MutableLiveData<>(0);
    public LiveData<Integer> getFailedCount() { return failedCount; }

    private final MutableLiveData<List<String>> logs = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> getLogs() { return logs; }

    private final MutableLiveData<TestResultSummary> resultSummary = new MutableLiveData<>();
    public LiveData<TestResultSummary> getResultSummary() { return resultSummary; }

    private TestConfig.TestItem[] testQueue;
    private int currentIndex = 0;
    private String currentSessionId;
    private List<TestResult> testResults = new ArrayList<>();

    public void setRepository(TestRepository repository) {
        this.repository = repository;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void prepareTest(TestConfig.TestItem[] items) {
        testQueue = items;
        currentIndex = 0;
        totalCount.setValue(items.length);
        progress.setValue(0);
        passedCount.setValue(0);
        failedCount.setValue(0);
        testResults.clear();
        addLog("准备好了 " + items.length + " 项测试");
    }

    public TestConfig.TestItem getCurrentTest() {
        if (testQueue == null || currentIndex >= testQueue.length) {
            return null;
        }
        return testQueue[currentIndex];
    }

    public void startTest() {
        if (testQueue == null || testQueue.length == 0) {
            addLog("错误：没有可执行的测试项");
            return;
        }

        testState.setValue(TestState.RUNNING);
        currentSessionId = repository.createSessionSync(
            getDeviceId(), android.os.Build.MODEL, android.os.Build.MANUFACTURER, testQueue.length);

        addLog("开始自动测试...");
        runNextTest();
    }

    public void pauseTest() {
        testState.setValue(TestState.PAUSED);
        addLog("测试已暂停");
    }

    public void resumeTest() {
        testState.setValue(TestState.RUNNING);
        addLog("测试继续");
        runNextTest();
    }

    public void stopTest() {
        testState.setValue(TestState.STOPPED);
        updateSessionSummary();
        addLog("测试已停止");
    }

    public void onTestResult(int testItemId, boolean passed) {
        if (testQueue == null) return;

        TestConfig.TestItem item = testQueue[currentIndex];
        if (item.getId() != testItemId) {
            addLog("警告：收到不匹配的测试结果");
            return;
        }

        // 记录结果
        TestResult result = new TestResult(
            item.getId(), item.getName(), passed ? "PASS" : "FAIL",
            getDeviceId(), android.os.Build.MODEL, android.os.Build.MANUFACTURER
        );
        testResults.add(result);

        if (passed) {
            passedCount.setValue(passedCount.getValue() + 1);
            addLog(item.getName() + ": PASS");
        } else {
            failedCount.setValue(failedCount.getValue() + 1);
            addLog(item.getName() + ": FAIL");
        }

        // 保存结果
        if (currentSessionId != null) {
            repository.saveTestResult(result, currentSessionId, id -> {});
        }

        currentIndex++;
        progress.setValue(currentIndex);

        // 检查是否完成
        if (currentIndex >= testQueue.length) {
            completeTest();
        } else {
            runNextTest();
        }
    }

    private void runNextTest() {
        if (testState.getValue() != TestState.RUNNING) return;
        if (currentIndex >= testQueue.length) {
            completeTest();
            return;
        }

        TestConfig.TestItem item = testQueue[currentIndex];
        currentTestName.setValue(item.getName());
        addLog("正在测试: " + item.getName());
    }

    private void completeTest() {
        testState.setValue(TestState.COMPLETED);
        updateSessionSummary();
        addLog("自动测试完成！");
        addLog("结果: " + passedCount.getValue() + " 通过, " + failedCount.getValue() + " 失败");
    }

    private void updateSessionSummary() {
        if (currentSessionId != null) {
            repository.updateSessionSummary(
                currentSessionId,
                passedCount.getValue(),
                failedCount.getValue(),
                0
            );
        }
    }

    private void addLog(String message) {
        List<String> currentLogs = logs.getValue();
        if (currentLogs == null) currentLogs = new ArrayList<>();
        currentLogs.add(message);
        // 限制日志数量
        if (currentLogs.size() > 100) {
            currentLogs = currentLogs.subList(currentLogs.size() - 100, currentLogs.size());
        }
        logs.setValue(currentLogs);
    }

    public void reset() {
        testState.setValue(TestState.IDLE);
        currentIndex = 0;
        progress.setValue(0);
        passedCount.setValue(0);
        failedCount.setValue(0);
        currentTestName.setValue("");
        logs.setValue(new ArrayList<>());
        testResults.clear();
        currentSessionId = null;
    }

    private String deviceId;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}

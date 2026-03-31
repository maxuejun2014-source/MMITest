package com.mxj.mmitest.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.mxj.mmitest.data.local.TestHistoryStore;
import com.mxj.mmitest.domain.model.TestResult;
import com.mxj.mmitest.domain.model.TestResultSummary;
import com.mxj.mmitest.domain.model.TestStatistics;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试数据仓库
 * 封装数据访问逻辑
 */
public class TestRepository {

    private static TestRepository instance;
    private final TestHistoryStore historyStore;
    private final Handler mainHandler;
    private final ExecutorService executor;

    private TestRepository(Context context) {
        this.historyStore = new TestHistoryStore(context.getApplicationContext());
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized TestRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TestRepository(context);
        }
        return instance;
    }

    // ===== 会话管理 =====

    /**
     * 创建新会话（在后台线程执行）
     */
    public void createSession(String deviceId, String deviceModel,
                             String deviceManufacturer, int totalCount,
                             Callback<String> callback) {
        executor.execute(() -> {
            String sessionId = historyStore.createSession(deviceId, deviceModel,
                deviceManufacturer, totalCount);
            postCallback(() -> callback.onSuccess(sessionId));
        });
    }

    /**
     * 同步创建新会话
     */
    public String createSessionSync(String deviceId, String deviceModel,
                                   String deviceManufacturer, int totalCount) {
        return historyStore.createSession(deviceId, deviceModel, deviceManufacturer, totalCount);
    }

    /**
     * 更新会话汇总
     */
    public void updateSessionSummary(String sessionId, int passed, int failed, int skipped) {
        executor.execute(() -> {
            historyStore.updateSessionSummary(sessionId, passed, failed, skipped);
        });
    }

    /**
     * 获取所有会话
     */
    public void getAllSessions(Callback<List<TestResultSummary>> callback) {
        executor.execute(() -> {
            List<TestResultSummary> sessions = historyStore.getAllSessions();
            postCallback(() -> callback.onSuccess(sessions));
        });
    }

    /**
     * 获取最近会话
     */
    public void getRecentSessions(int limit, Callback<List<TestResultSummary>> callback) {
        executor.execute(() -> {
            List<TestResultSummary> sessions = historyStore.getRecentSessions(limit);
            postCallback(() -> callback.onSuccess(sessions));
        });
    }

    /**
     * 获取指定会话
     */
    public void getSession(String sessionId, Callback<TestResultSummary> callback) {
        executor.execute(() -> {
            TestResultSummary session = historyStore.getSession(sessionId);
            postCallback(() -> callback.onSuccess(session));
        });
    }

    /**
     * 获取会话的所有测试结果
     */
    public void getSessionResults(String sessionId, Callback<List<TestResult>> callback) {
        executor.execute(() -> {
            List<TestResult> results = historyStore.getSessionResults(sessionId);
            postCallback(() -> callback.onSuccess(results));
        });
    }

    /**
     * 保存测试结果
     */
    public void saveTestResult(TestResult result, String sessionId, Callback<Long> callback) {
        executor.execute(() -> {
            historyStore.saveTestResult(result, sessionId);
            postCallback(() -> callback.onSuccess(0L));
        });
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        executor.execute(() -> {
            historyStore.deleteSession(sessionId);
        });
    }

    /**
     * 清空所有数据
     */
    public void deleteAll() {
        executor.execute(() -> {
            historyStore.deleteAll();
        });
    }

    // ===== 单项测试 =====

    /**
     * 保存单项测试结果
     */
    public void saveSingleTestResult(int testItemId, String testItemName,
                                     boolean passed, String deviceId) {
        executor.execute(() -> {
            historyStore.saveSingleTestResult(testItemId, testItemName, passed, deviceId);
        });
    }

    /**
     * 获取单项测试状态
     */
    public void getSingleTestStatus(int testItemId, Callback<TestHistoryStore.TestStatus> callback) {
        executor.execute(() -> {
            TestHistoryStore.TestStatus status = historyStore.getSingleTestStatus(testItemId);
            postCallback(() -> callback.onSuccess(status));
        });
    }

    /**
     * 同步保存单项测试结果
     */
    public void saveSingleTestResultSync(int testItemId, String testItemName,
                                        boolean passed, String deviceId) {
        historyStore.saveSingleTestResult(testItemId, testItemName, passed, deviceId);
    }

    /**
     * 同步获取单项测试状态
     */
    public TestHistoryStore.TestStatus getSingleTestStatusSync(int testItemId) {
        return historyStore.getSingleTestStatus(testItemId);
    }

    // ===== 统计 =====

    /**
     * 获取统计数据
     */
    public void getStatistics(Callback<TestStatistics> callback) {
        executor.execute(() -> {
            List<TestResultSummary> sessions = historyStore.getAllSessions();
            TestStatistics stats = new TestStatistics();
            stats.setTotalSessions(sessions.size());

            int totalTests = 0, passedTests = 0, failedTests = 0, skippedTests = 0;
            TestResultSummary lastSession = null;

            for (TestResultSummary session : sessions) {
                totalTests += session.getTotalCount();
                passedTests += session.getPassedCount();
                failedTests += session.getFailedCount();
                skippedTests += session.getSkippedCount();
                lastSession = session;
            }

            stats.setTotalTests(totalTests);
            stats.setPassedTests(passedTests);
            stats.setFailedTests(failedTests);
            stats.setSkippedTests(skippedTests);
            stats.setLastSession(lastSession);
            stats.calculate();

            postCallback(() -> callback.onSuccess(stats));
        });
    }

    // ===== 辅助方法 =====

    private void postCallback(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }

    /**
     * 回调接口
     */
    public interface Callback<T> {
        void onSuccess(T result);
    }
}

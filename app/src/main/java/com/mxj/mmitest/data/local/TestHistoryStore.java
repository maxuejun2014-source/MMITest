package com.mxj.mmitest.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.mxj.mmitest.domain.model.TestResult;
import com.mxj.mmitest.domain.model.TestResultSummary;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 测试历史数据存储
 * 使用 SharedPreferences + JSON 文件替代 Room 数据库
 */
public class TestHistoryStore {

    private static final String PREFS_NAME = "mmitest_history";
    private static final String KEY_SESSIONS = "sessions"; // JSON array of session IDs
    private static final String DIR_RESULTS = "test_results";
    private static final String KEY_SINGLE_TEST_PREFIX = "single_test_";

    private final Context context;
    private final SharedPreferences prefs;

    public TestHistoryStore(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureResultsDir();
    }

    private void ensureResultsDir() {
        File dir = new File(context.getFilesDir(), DIR_RESULTS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private File getResultsDir() {
        return new File(context.getFilesDir(), DIR_RESULTS);
    }

    // ===== 会话管理 =====

    /**
     * 创建新会话
     */
    public String createSession(String deviceId, String deviceModel,
                               String deviceManufacturer, int totalCount) {
        String sessionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        JSONObject session = new JSONObject();
        try {
            session.put("sessionId", sessionId);
            session.put("deviceId", deviceId);
            session.put("deviceModel", deviceModel);
            session.put("deviceManufacturer", deviceManufacturer);
            session.put("startTime", startTime);
            session.put("totalCount", totalCount);
            session.put("passedCount", 0);
            session.put("failedCount", 0);
            session.put("skippedCount", 0);
            session.put("operator", "工厂测试员");
            session.put("appVersion", "1.0.0");
            session.put("endTime", 0);

            saveSessionToFile(sessionId, session);
            addSessionToList(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sessionId;
    }

    /**
     * 更新会话汇总
     */
    public void updateSessionSummary(String sessionId, int passed, int failed, int skipped) {
        JSONObject session = loadSessionFromFile(sessionId);
        if (session != null) {
            try {
                session.put("endTime", System.currentTimeMillis());
                session.put("passedCount", passed);
                session.put("failedCount", failed);
                session.put("skippedCount", skipped);
                saveSessionToFile(sessionId, session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取所有会话
     */
    public List<TestResultSummary> getAllSessions() {
        List<TestResultSummary> sessions = new ArrayList<>();
        String sessionIdsJson = prefs.getString(KEY_SESSIONS, "[]");
        try {
            JSONArray ids = new JSONArray(sessionIdsJson);
            for (int i = ids.length() - 1; i >= 0; i--) { // 逆序，最新的在前
                String sessionId = ids.getString(i);
                JSONObject session = loadSessionFromFile(sessionId);
                if (session != null) {
                    sessions.add(jsonToSummary(session));
                } else {
                    // 会话文件不存在，从列表移除
                    removeSessionFromList(sessionId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessions;
    }

    /**
     * 获取最近N条会话
     */
    public List<TestResultSummary> getRecentSessions(int limit) {
        List<TestResultSummary> all = getAllSessions();
        if (all.size() <= limit) return all;
        return all.subList(0, limit);
    }

    /**
     * 获取指定会话
     */
    public TestResultSummary getSession(String sessionId) {
        JSONObject session = loadSessionFromFile(sessionId);
        if (session == null) return null;
        return jsonToSummary(session);
    }

    /**
     * 获取会话的所有测试结果
     */
    public List<TestResult> getSessionResults(String sessionId) {
        List<TestResult> results = new ArrayList<>();
        File resultsFile = getResultsFile(sessionId);
        if (!resultsFile.exists()) return results;

        try {
            String content = readFileContent(resultsFile);
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                results.add(jsonToResult(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 保存测试结果到会话
     */
    public void saveTestResult(TestResult result, String sessionId) {
        result.setSessionId(sessionId);
        List<TestResult> results = getSessionResults(sessionId);
        results.add(result);
        saveResultsToFile(sessionId, results);
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        removeSessionFromList(sessionId);
        File sessionFile = getSessionFile(sessionId);
        File resultsFile = getResultsFile(sessionId);
        if (sessionFile.exists()) sessionFile.delete();
        if (resultsFile.exists()) resultsFile.delete();
    }

    /**
     * 清空所有数据
     */
    public void deleteAll() {
        try {
            File dir = getResultsDir();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            prefs.edit().remove(KEY_SESSIONS).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 单项测试结果 =====

    /**
     * 保存单项测试结果
     */
    public void saveSingleTestResult(int testItemId, String testItemName,
                                     boolean passed, String deviceId) {
        String key = KEY_SINGLE_TEST_PREFIX + testItemId;
        JSONObject result = new JSONObject();
        try {
            result.put("testItemId", testItemId);
            result.put("testItemName", testItemName);
            result.put("result", passed ? "PASS" : "FAIL");
            result.put("timestamp", System.currentTimeMillis());
            result.put("deviceId", deviceId);
            prefs.edit().putString(key, result.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取单项测试最新结果
     */
    public String getSingleTestResult(int testItemId) {
        String key = KEY_SINGLE_TEST_PREFIX + testItemId;
        return prefs.getString(key, null);
    }

    /**
     * 获取单项测试结果状态
     */
    public TestStatus getSingleTestStatus(int testItemId) {
        String json = getSingleTestResult(testItemId);
        if (json == null) return new TestStatus(testItemId, null, null, 0, null);
        try {
            JSONObject obj = new JSONObject(json);
            return new TestStatus(
                obj.getInt("testItemId"),
                obj.optString("testItemName", ""),
                obj.optString("result", ""),
                obj.optLong("timestamp", 0),
                obj.optString("deviceId", null)
            );
        } catch (Exception e) {
            return new TestStatus(testItemId, null, null, 0, null);
        }
    }

    // ===== 辅助方法 =====

    private File getSessionFile(String sessionId) {
        return new File(getResultsDir(), "session_" + sessionId + ".json");
    }

    private File getResultsFile(String sessionId) {
        return new File(getResultsDir(), "results_" + sessionId + ".json");
    }

    private void saveSessionToFile(String sessionId, JSONObject session) {
        try {
            File file = getSessionFile(sessionId);
            FileWriter writer = new FileWriter(file);
            writer.write(session.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject loadSessionFromFile(String sessionId) {
        try {
            File file = getSessionFile(sessionId);
            if (file.exists()) {
                String content = readFileContent(file);
                return new JSONObject(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveResultsToFile(String sessionId, List<TestResult> results) {
        try {
            JSONArray array = new JSONArray();
            for (TestResult result : results) {
                array.put(resultToJson(result));
            }
            File file = getResultsFile(sessionId);
            FileWriter writer = new FileWriter(file);
            writer.write(array.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFileContent(File file) throws Exception {
        FileReader reader = new FileReader(file);
        char[] buffer = new char[(int) file.length()];
        reader.read(buffer);
        reader.close();
        return new String(buffer);
    }

    private void addSessionToList(String sessionId) {
        try {
            String json = prefs.getString(KEY_SESSIONS, "[]");
            JSONArray ids = new JSONArray(json);
            ids.put(sessionId);
            prefs.edit().putString(KEY_SESSIONS, ids.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeSessionFromList(String sessionId) {
        try {
            String json = prefs.getString(KEY_SESSIONS, "[]");
            JSONArray ids = new JSONArray(json);
            JSONArray newIds = new JSONArray();
            for (int i = 0; i < ids.length(); i++) {
                if (!ids.getString(i).equals(sessionId)) {
                    newIds.put(ids.getString(i));
                }
            }
            prefs.edit().putString(KEY_SESSIONS, newIds.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TestResultSummary jsonToSummary(JSONObject obj) {
        TestResultSummary summary = new TestResultSummary();
        try {
            summary.setSessionId(obj.getString("sessionId"));
            summary.setDeviceId(obj.optString("deviceId", ""));
            summary.setDeviceModel(obj.optString("deviceModel", ""));
            summary.setDeviceManufacturer(obj.optString("deviceManufacturer", ""));
            summary.setStartTime(obj.optLong("startTime", 0));
            summary.setEndTime(obj.optLong("endTime", 0));
            summary.setTotalCount(obj.optInt("totalCount", 0));
            summary.setPassedCount(obj.optInt("passedCount", 0));
            summary.setFailedCount(obj.optInt("failedCount", 0));
            summary.setSkippedCount(obj.optInt("skippedCount", 0));
            summary.setOperator(obj.optString("operator", ""));
            summary.setAppVersion(obj.optString("appVersion", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }

    private JSONObject resultToJson(TestResult result) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", result.getId());
            obj.put("sessionId", result.getSessionId());
            obj.put("testItemId", result.getTestItemId());
            obj.put("testItemName", result.getTestItemName());
            obj.put("result", result.getResult());
            obj.put("timestamp", result.getTimestamp());
            obj.put("deviceId", result.getDeviceId());
            obj.put("deviceModel", result.getDeviceModel());
            obj.put("deviceManufacturer", result.getDeviceManufacturer());
            obj.put("additionalInfo", result.getAdditionalInfo());
            obj.put("timeoutReason", result.getTimeoutReason());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private TestResult jsonToResult(JSONObject obj) {
        TestResult result = new TestResult();
        try {
            result.setId(obj.optLong("id", 0));
            result.setSessionId(obj.optString("sessionId", ""));
            result.setTestItemId(obj.optInt("testItemId", 0));
            result.setTestItemName(obj.optString("testItemName", ""));
            result.setResult(obj.optString("result", ""));
            result.setTimestamp(obj.optLong("timestamp", 0));
            result.setDeviceId(obj.optString("deviceId", ""));
            result.setDeviceModel(obj.optString("deviceModel", ""));
            result.setDeviceManufacturer(obj.optString("deviceManufacturer", ""));
            result.setAdditionalInfo(obj.optString("additionalInfo", "{}"));
            result.setTimeoutReason(obj.optString("timeoutReason", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 测试状态内部类
     */
    public static class TestStatus {
        public final int testItemId;
        public final String testItemName;
        public final String result;
        public final long timestamp;
        public final String deviceId;

        public TestStatus(int testItemId, String testItemName, String result,
                         long timestamp, String deviceId) {
            this.testItemId = testItemId;
            this.testItemName = testItemName;
            this.result = result;
            this.timestamp = timestamp;
            this.deviceId = deviceId;
        }

        public boolean isPassed() {
            return "PASS".equals(result);
        }

        public boolean isFailed() {
            return "FAIL".equals(result);
        }
    }
}

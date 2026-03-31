package com.mxj.mmitest.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.domain.model.TestResultSummary;
import com.mxj.mmitest.domain.model.TestStatistics;
import com.mxj.mmitest.domain.model.TestResult;
import java.util.List;

/**
 * 结果ViewModel
 */
public class ResultViewModel extends ViewModel {

    public enum ResultTab {
        HISTORY, STATISTICS, QR_CODE
    }

    private final MutableLiveData<ResultTab> currentTab = new MutableLiveData<>(ResultTab.HISTORY);
    public LiveData<ResultTab> getCurrentTab() { return currentTab; }

    private final MutableLiveData<List<TestResultSummary>> sessions = new MutableLiveData<>();
    public LiveData<List<TestResultSummary>> getSessions() { return sessions; }

    private final MutableLiveData<String> selectedSessionId = new MutableLiveData<>();
    public LiveData<String> getSelectedSessionId() { return selectedSessionId; }

    private final MutableLiveData<TestResultSummary> selectedSummary = new MutableLiveData<>();
    public LiveData<TestResultSummary> getSelectedSummary() { return selectedSummary; }

    private final MutableLiveData<List<TestResult>> selectedResults = new MutableLiveData<>();
    public LiveData<List<TestResult>> getSelectedResults() { return selectedResults; }

    private final MutableLiveData<TestStatistics> statistics = new MutableLiveData<>();
    public LiveData<TestStatistics> getStatistics() { return statistics; }

    private final MutableLiveData<String> qrCodeData = new MutableLiveData<>();
    public LiveData<String> getQrCodeData() { return qrCodeData; }

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    private final MutableLiveData<String> exportMessage = new MutableLiveData<>();
    public LiveData<String> getExportMessage() { return exportMessage; }

    private TestRepository repository;

    public void setRepository(TestRepository repository) {
        this.repository = repository;
    }

    public void loadSessions() {
        if (repository == null) return;
        isLoading.setValue(true);
        repository.getAllSessions(results -> {
            sessions.setValue(results);
            isLoading.setValue(false);
        });
    }

    public void selectSession(String sessionId) {
        selectedSessionId.setValue(sessionId);
        if (repository != null && sessionId != null) {
            repository.getSession(sessionId, summary -> {
                selectedSummary.setValue(summary);
                generateQrCodeData(summary);
            });
            repository.getSessionResults(sessionId, results -> {
                selectedResults.setValue(results);
            });
        }
    }

    public void deleteSession(String sessionId) {
        if (repository != null) {
            repository.deleteSession(sessionId);
            if (sessionId.equals(selectedSessionId.getValue())) {
                selectedSessionId.setValue(null);
                selectedSummary.setValue(null);
                selectedResults.setValue(null);
            }
            loadSessions();
        }
    }

    public void loadStatistics() {
        if (repository == null) return;
        repository.getStatistics(stats -> {
            statistics.setValue(stats);
        });
    }

    public void setTab(ResultTab tab) {
        currentTab.setValue(tab);
        if (tab == ResultTab.STATISTICS) {
            loadStatistics();
        }
    }

    private void generateQrCodeData(TestResultSummary summary) {
        if (summary == null) {
            qrCodeData.setValue(null);
            return;
        }
        // 生成JSON格式的汇总数据
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"device\":\"").append(summary.getDeviceModel()).append("\",");
        sb.append("\"manufacturer\":\"").append(summary.getDeviceManufacturer()).append("\",");
        sb.append("\"total\":").append(summary.getTotalCount()).append(",");
        sb.append("\"passed\":").append(summary.getPassedCount()).append(",");
        sb.append("\"failed\":").append(summary.getFailedCount()).append(",");
        sb.append("\"time\":\"").append(summary.getFormattedDuration()).append("\"");
        sb.append("}");
        qrCodeData.setValue(sb.toString());
    }
}

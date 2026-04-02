package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;
import java.util.concurrent.ExecutionException;

/**
 * 后置摄像头测试 - 使用CameraX
 */
public class RearCameraTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 17;
    private static final int TIMEOUT_SECONDS = 60;

    private TestRepository repository;
    private PreviewView mPreviewView;
    private ProcessCameraProvider mCameraProvider;
    private Camera mCamera;
    private TextView mTextInfo;
    private boolean mCameraOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        mPreviewView = new PreviewView(this);
        mPreviewView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(mPreviewView);

        mTextInfo = new TextView(this);
        mTextInfo.setText("Rear Camera Test");
        mTextInfo.setTextColor(0xFFFFFFFF);
        mTextInfo.setTextSize(16);
        mTextInfo.setPadding(20, 20, 20, 20);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
        mTextInfo.setLayoutParams(textParams);
        root.addView(mTextInfo);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && allPermissionsGranted()) {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                mCameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                mTextInfo.setText("Camera initialization failed");
                setPassEnabled(false);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (mCameraProvider == null || !(this instanceof LifecycleOwner)) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        try {
            mCameraProvider.unbindAll();
            mCamera = mCameraProvider.bindToLifecycle(
                    (LifecycleOwner) this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview);
            mCameraOpened = true;
            mTextInfo.setText("Rear Camera OK");
            setPassEnabled(true);
        } catch (Exception e) {
            mTextInfo.setText("Camera binding failed: " + e.getMessage());
            setPassEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraProvider != null) {
            mCameraProvider.unbindAll();
        }
    }

    @Override
    protected String getTestName() {
        return "后置摄像头测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查后置摄像头画面\n\n观察画面是否清晰正常";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return mCameraOpened;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }
}

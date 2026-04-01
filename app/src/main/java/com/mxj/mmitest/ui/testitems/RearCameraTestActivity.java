package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * 后置摄像头测试
 * 超时60秒，需要打开后置摄像头预览
 */
public class RearCameraTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 17;
    private static final int TIMEOUT_SECONDS = 60;

    private TestRepository repository;
    private LinearLayout contentLayout;
    private SurfaceView surfaceView;
    private TextView statusTextView;
    private SurfaceHolder surfaceHolder;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private String rearCameraId;
    private Size previewSize;

    private boolean isCameraOpened = false;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("后置摄像头测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 16);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在初始化摄像头...\n");
        statusTextView.setTextSize(16);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        // 添加SurfaceView用于预览
        surfaceView = new SurfaceView(this);
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1
        ));

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // 尺寸变化处理
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                closeCamera();
            }
        });

        contentLayout.addView(surfaceView);
        setContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "后置摄像头测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查后置摄像头画面\n\n操作步骤：\n1. 观察摄像头预览画面\n2. 确认画面清晰无异常\n3. 点击PASS或FAIL按钮";
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
        updateStatus("正在检测后置摄像头...\n");
    }

    private void openCamera() {
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            // 获取后置摄像头ID
            String[] cameraIds = cameraManager.getCameraIdList();
            rearCameraId = null;

            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    rearCameraId = id;
                    break;
                }
            }

            if (rearCameraId == null) {
                updateStatus("未找到后置摄像头\n");
                setPassEnabled(false);
                return;
            }

            // 获取预览尺寸
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(rearCameraId);
            previewSize = getOptimalPreviewSize(characteristics, surfaceHolder.getSurfaceFrame().width(),
                surfaceHolder.getSurfaceFrame().height());

            if (previewSize == null) {
                previewSize = new Size(1920, 1080);
            }

            updateStatus("后置摄像头: 已找到\n");
            updateStatus("预览尺寸: " + previewSize.getWidth() + "x" + previewSize.getHeight() + "\n");

            // 打开摄像头
            cameraManager.openCamera(rearCameraId, stateCallback, mainHandler);

        } catch (CameraAccessException e) {
            updateStatus("摄像头访问失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        } catch (SecurityException e) {
            updateStatus("摄像头权限被拒绝\n");
            setPassEnabled(false);
        }
    }

    private Size getOptimalPreviewSize(CameraCharacteristics characteristics, int width, int height) {
        Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            .getOutputSizes(SurfaceHolder.class);

        if (sizes == null || sizes.length == 0) {
            return null;
        }

        Size targetSize = new Size(width, height);
        Size result = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : sizes) {
            if (size.getWidth() <= 1920 && size.getHeight() <= 1080) {
                double diff = Math.abs(size.getWidth() - targetSize.getWidth())
                    + Math.abs(size.getHeight() - targetSize.getHeight());
                if (diff < minDiff) {
                    minDiff = diff;
                    result = size;
                }
            }
        }

        return result != null ? result : sizes[0];
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            isCameraOpened = true;
            updateStatus("摄像头已打开\n");
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
            updateStatus("摄像头已断开连接\n");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            updateStatus("摄像头错误: " + error + "\n");
            setPassEnabled(false);
        }
    };

    private void createCameraPreviewSession() {
        try {
            if (cameraDevice == null || !surfaceHolder.getSurface().isValid()) {
                return;
            }

            List<Surface> surfaces = new ArrayList<>();
            Surface previewSurface = surfaceHolder.getSurface();
            surfaces.add(previewSurface);

            // 创建预览请求
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);

            // 创建捕获会话
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    captureSession = session;
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, mainHandler);
                        updateStatus("预览画面显示正常\n");
                        setPassEnabled(true);
                    } catch (CameraAccessException e) {
                        updateStatus("预览失败: " + e.getMessage() + "\n");
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    updateStatus("摄像头配置失败\n");
                    setPassEnabled(false);
                }
            }, mainHandler);

        } catch (CameraAccessException e) {
            updateStatus("创建预览会话失败: " + e.getMessage() + "\n");
        }
    }

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    @Override
    protected boolean isPassEnabled() {
        return isCameraOpened;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
    }
}

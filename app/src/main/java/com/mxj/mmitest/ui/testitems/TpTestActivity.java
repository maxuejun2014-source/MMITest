package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * TP触摸屏测试
 * 超时45秒，网格24x15，用户触摸后格子变黄色，所有需要检测的格子被触摸后自动通过
 */
public class TpTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 8;
    private static final int TIMEOUT_SECONDS = 45;
    private static final int YCOUNT = 24; // 横向网格数（行数）
    private static final int XCOUNT = 15; // 纵向网格数（列数）

    private TestRepository repository;
    private TpTestView tpTestView;
    private Handler countdownHandler;
    private int remainingSeconds;
    private boolean testCompleted = false;

    // 颜色配置
    private static final int GRID_LINE_COLOR = Color.BLACK;
    private static final int GRID_FILL_COLOR = Color.parseColor("#FFC107"); // 黄色
    private static final int TOUCH_PATH_COLOR = Color.parseColor("#4CAF50"); // 绿色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        countdownHandler = new Handler(Looper.getMainLooper());
        remainingSeconds = TIMEOUT_SECONDS;

        tpTestView = new TpTestView(this);
        setContentView(tpTestView);

        startCountdown();
    }

    private void startCountdown() {
        countdownHandler.post(new Runnable() {
            @Override
            public void run() {
                remainingSeconds--;
                tpTestView.updateCountdown(remainingSeconds);

                if (remainingSeconds <= 0) {
                    if (!testCompleted) {
                        testCompleted = true;
                        saveAndFinish(false);
                    }
                } else {
                    countdownHandler.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    protected String getTestName() {
        return "TP测试";
    }

    @Override
    protected String getTestDescription() {
        return "请在屏幕上触摸所有显示的网格\n\n操作步骤：\n1. 触摸屏幕上显示的所有网格线\n2. 确保每个需要检测的格子都变为黄色\n3. 全部触摸后自动判定通过";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{}; // 无需特殊权限
    }

    @Override
    protected void onTestExecute() {
        // 无需额外执行
    }

    @Override
    protected boolean isPassEnabled() {
        return tpTestView != null && tpTestView.isGridPassed();
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    private void saveAndFinish(boolean passed) {
        repository.saveSingleTestResult(
            TEST_ITEM_ID,
            getTestName(),
            passed,
            getDeviceUniqueId()
        );
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null) {
            countdownHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 自定义TP测试视图
     */
    private class TpTestView extends View {
        private Paint gridLinePaint;
        private Paint gridFillPaint;
        private Paint touchPathPaint;
        private Paint textPaint;

        private boolean[][] drawnGrid; // 记录每个格子是否被触摸
        private List<float[]> touchPath; // 触摸路径
        private float stepX;
        private float stepY;
        private int countdown = TIMEOUT_SECONDS;

        public TpTestView(Context context) {
            super(context);
            initPaints();
            drawnGrid = new boolean[YCOUNT][XCOUNT];
            touchPath = new ArrayList<>();
        }

        private void initPaints() {
            gridLinePaint = new Paint();
            gridLinePaint.setColor(GRID_LINE_COLOR);
            gridLinePaint.setStyle(Paint.Style.STROKE);
            gridLinePaint.setStrokeWidth(2f);

            gridFillPaint = new Paint();
            gridFillPaint.setColor(GRID_FILL_COLOR);
            gridFillPaint.setStyle(Paint.Style.FILL);

            touchPathPaint = new Paint();
            touchPathPaint.setColor(TOUCH_PATH_COLOR);
            touchPathPaint.setStyle(Paint.Style.STROKE);
            touchPathPaint.setStrokeWidth(8f);
            touchPathPaint.setStrokeCap(Paint.Cap.ROUND);
            touchPathPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(40f);
            textPaint.setAntiAlias(true);
        }

        public void updateCountdown(int seconds) {
            this.countdown = seconds;
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            stepX = w / (float) XCOUNT;
            stepY = h / (float) YCOUNT;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();
            stepX = width / (float) XCOUNT;
            stepY = height / (float) YCOUNT;

            // 绘制网格线和填充
            for (int i = 0; i < YCOUNT; i++) {
                for (int j = 0; j < XCOUNT; j++) {
                    if (isNeedCheck(i, j)) {
                        float left = j * stepX + 1;
                        float top = i * stepY + 1;
                        float right = left + stepX - 2;
                        float bottom = top + stepY - 2;

                        RectF rect = new RectF(left, top, right, bottom);

                        // 已填充的格子显示黄色
                        if (drawnGrid[i][j]) {
                            canvas.drawRect(rect, gridFillPaint);
                        }

                        // 绘制黑色边框
                        canvas.drawRect(rect, gridLinePaint);
                    }
                }
            }

            // 绘制触摸路径（绿色）
            if (touchPath.size() > 1) {
                for (int i = 0; i < touchPath.size() - 1; i++) {
                    float[] p1 = touchPath.get(i);
                    float[] p2 = touchPath.get(i + 1);
                    canvas.drawLine(p1[0], p1[1], p2[0], p2[1], touchPathPaint);
                }
            }

            // 绘制倒计时
            String countdownText = "剩余时间: " + countdown + "秒";
            if (countdown <= 5) {
                textPaint.setColor(Color.RED);
            } else {
                textPaint.setColor(Color.DKGRAY);
            }
            canvas.drawText(countdownText, 20, 50, textPaint);

            // 绘制提示文字
            String hintText = "请触摸所有网格";
            textPaint.setColor(Color.BLUE);
            canvas.drawText(hintText, width / 2 - 120, height - 50, textPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (testCompleted) {
                return true;
            }

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchPath.clear();
                    touchPath.add(new float[]{x, y});
                    updateGrid(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchPath.add(new float[]{x, y});
                    updateGrid(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    touchPath.clear();
                    break;
            }

            invalidate();

            // 检查是否通过
            if (isGridPassed()) {
                testCompleted = true;
                saveAndFinish(true);
            }

            return true;
        }

        private void updateGrid(float touchX, float touchY) {
            int col = (int) (touchX / stepX);
            int row = (int) (touchY / stepY);

            if (col >= 0 && col < XCOUNT && row >= 0 && row < YCOUNT) {
                if (isNeedCheck(row, col)) {
                    drawnGrid[row][col] = true;
                }
            }
        }

        // 判断该位置是否需要检测（参考MTK原厂代码）
        private boolean isNeedCheck(int y, int x) {
            return (y == 0 // 顶部
                    || y == (YCOUNT - 1) // 底部
                    || y == (YCOUNT / 5) // Y方向1/5处
                    || y == (YCOUNT * 2 / 5) // Y方向2/5处
                    || y == (YCOUNT * 3 / 5) // Y方向3/5处
                    || y == (YCOUNT * 4 / 5) // Y方向4/5处
                    || x == 0 // 左边
                    || x == (XCOUNT - 1) // 右边
                    || x == (XCOUNT / 2) // X方向中间
            );
        }

        // 判断是否所有需要检测的格子都被绘制
        public boolean isGridPassed() {
            for (int i = 0; i < YCOUNT; i++) {
                for (int j = 0; j < XCOUNT; j++) {
                    if (isNeedCheck(i, j) && !drawnGrid[i][j]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}

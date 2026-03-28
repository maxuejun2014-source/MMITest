package com.mxj.mmitest.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 基础Activity类
 * 所有Activity继承此类，实现全屏显示功能
 */
abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
    }

    /**
     * 启用全屏显示
     * 隐藏状态栏和导航栏
     */
    protected fun enableFullScreen() {
        // 设置窗口不使用系统窗口装饰
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 获取窗口Insets控制器
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 隐藏系统栏（状态栏和导航栏）
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // 设置系统栏行为：滑动时临时显示
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 窗口获得焦点时再次启用全屏，确保从其他页面返回时也是全屏
            enableFullScreen()
        }
    }
}

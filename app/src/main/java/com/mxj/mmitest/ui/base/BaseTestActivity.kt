package com.mxj.mmitest.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.util.PermissionUtils
import kotlinx.coroutines.launch

/**
 * 测试项Activity基类
 * 提供统一的超时处理和权限管理功能
 *
 * 使用方式：
 * class SimTestActivity : BaseTestActivity() {
 *     override val testName = "SIM卡测试"
 *     override val testDescription = "请检查SIM卡是否正常识别"
 *     override val timeoutSeconds = 15
 *     override val requiredPermissions = listOf(Manifest.permission.READ_PHONE_STATE)
 *
 *     override fun onTestExecute() {
 *         // 实现测试逻辑
 *     }
 * }
 */
abstract class BaseTestActivity : BaseActivity() {

    /** 测试项名称 */
    abstract val testName: String

    /** 测试项描述 */
    abstract val testDescription: String

    /** 超时时间（秒） */
    open val timeoutSeconds: Int = 60

    /** 所需权限列表 */
    open val requiredPermissions: List<String> = emptyList()

    /** 是否需要自动保存结果 */
    open val autoSaveResult: Boolean = true

    /** 测试结果回调 - 供子类实现 */
    var onTestResult: ((Boolean) -> Unit)? = null

    private var remainingSeconds by mutableIntStateOf(timeoutSeconds)
    private var showPermissionDialog by mutableStateOf(false)
    private var missingPermissions by mutableStateOf<List<String>>(emptyList())

    // PASS按钮是否可点击（测试条件满足时由子类调用enablePassButton()启用）
    private var passButtonEnabled by mutableStateOf(false)

    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    // 权限请求Launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            missingPermissions = permissions.filter { !it.value }.keys.toList()
            showPermissionDialog = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查权限
        if (requiredPermissions.isNotEmpty()) {
            val missing = PermissionUtils.getMissingPermissions(this, requiredPermissions)
            if (missing.isNotEmpty()) {
                missingPermissions = missing
                showPermissionDialog = true
            } else {
                startTest()
            }
        } else {
            startTest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdown()
    }

    /** 开始测试 */
    private fun startTest() {
        setContent {
            MaterialTheme {
                TestContent()
            }
        }
        startCountdown()
    }

    /** 测试内容 */
    @Composable
    private fun TestContent() {
        var localRemainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }

        // 监听remainingSeconds变化
        LaunchedEffect(remainingSeconds) {
            localRemainingSeconds = remainingSeconds
        }

        TestItemScreen(
            testName = testName,
            testDescription = testDescription,
            remainingSeconds = localRemainingSeconds,
            onPass = { handleTestResult(true) },
            onFail = { handleTestResult(false) },
            passEnabled = passButtonEnabled
        )

        // 权限缺失对话框
        if (showPermissionDialog) {
            PermissionDeniedDialog(
                permissions = missingPermissions.map { PermissionUtils.getPermissionDisplayName(it) },
                onOpenSettings = {
                    showPermissionDialog = false
                    openAppSettings()
                },
                onCancel = {
                    showPermissionDialog = false
                    handleTestResult(false)
                }
            )
        }
    }

    /** 开始倒计时 */
    private fun startCountdown() {
        stopCountdown()
        countdownRunnable = object : Runnable {
            override fun run() {
                remainingSeconds--
                if (remainingSeconds <= 0) {
                    // 超时直接设置FAIL，不弹框
                    handleTestResult(false)
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(countdownRunnable!!)
    }

    /** 启用PASS按钮（测试条件满足时调用） */
    protected fun enablePassButton() {
        passButtonEnabled = true
    }

    /** 停止倒计时 */
    private fun stopCountdown() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable = null
    }

    /** 处理测试结果 */
    private fun handleTestResult(isPass: Boolean) {
        stopCountdown()
        onTestResult?.invoke(isPass)
        finish()
    }

    /** 权限授予后开始测试 */
    protected open fun onPermissionsGranted() {
        startTest()
    }

    /** 打开应用设置页面 */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}

/**
 * 权限被拒绝对话框
 */
@Composable
fun PermissionDeniedDialog(
    permissions: List<String>,
    onOpenSettings: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("权限不足") },
        text = {
            Column {
                Text("以下权限是测试所必需的，但未被授予：")
                Spacer(modifier = Modifier.height(8.dp))
                permissions.forEach { permission ->
                    Text("• $permission", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("请在设置中授予相关权限。")
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("打开设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

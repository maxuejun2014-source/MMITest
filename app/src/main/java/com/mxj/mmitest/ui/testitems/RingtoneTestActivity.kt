package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.media.MediaPlayer
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RingtoneTestActivity : BaseActivity() {
    private val testName = "铃声测试"
    private val timeoutSeconds = 20
    private val testItemId = 11
    private lateinit var repository: TestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(true) }
            TestItemScreen(
                testName = testName,
                testDescription = "扬声器播放铃声测试\n\n请注意听扬声器声音\n点击PASS表示声音正常，FAIL表示无声或异常",
                remainingSeconds = remainingSeconds,
                passEnabled = passEnabled,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) }
            )
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束
                saveAndFinish(false)
            }
        }
    }

    private fun saveAndFinish(passed: Boolean) {
        lifecycleScope.launch {
            repository.saveSingleTestResult(
                testItemId = testItemId,
                testItemName = testName,
                passed = passed,
                deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: android.os.Build.MODEL
            )
            finish()
        }
    }
}

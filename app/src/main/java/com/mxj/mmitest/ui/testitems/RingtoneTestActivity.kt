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
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

class RingtoneTestActivity : BaseActivity() {
    private val testName = "铃声测试"
    private val timeoutSeconds = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            TestItemScreen(
                testName = testName,
                testDescription = "扬声器播放铃声测试\n\n请注意听扬声器声音\n点击PASS表示声音正常，FAIL表示无声或异常",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束
                finish()
            }
        }
    }
}

package com.mxj.mmitest.ui.autotest

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxj.mmitest.ui.base.BaseActivity

/**
 * 自动测试界面Activity
 */
class AutoTestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val viewModel: AutoTestViewModel = viewModel()
                AutoTestScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onTestComplete = {
                        // 测试完成，跳转到结果页面
                        finish()
                    }
                )
            }
        }
    }
}

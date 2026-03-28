package com.mxj.mmitest.ui.autotest

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxj.mmitest.ui.base.BaseActivity

/**
 * 自动测试界面Activity
 */
class AutoTestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: AutoTestViewModel = viewModel()
            AutoTestScreen(
                viewModel = viewModel,
                onBackClick = { finish() }
            )
        }
    }
}

/**
 * 自动测试界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTestScreen(
    viewModel: AutoTestViewModel,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自动测试") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "自动测试功能开发中...",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "共 ${viewModel.totalCount} 项测试",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

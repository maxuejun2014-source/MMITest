package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SIM卡测试Activity
 */
class SimTestActivity : BaseActivity() {

    private val testName = "SIM卡测试"
    private val testItemId = 1
    private val timeoutSeconds = 15

    private lateinit var repository: TestRepository
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager

    // SIM卡信息数据类
    data class SimCardInfo(
        val slotIndex: Int,
        val displayName: String,
        val carrierName: String,
        val simSerialNumber: String,
        val isReady: Boolean,
        val simState: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        telephonyManager = getSystemService<TelephonyManager>()!!
        subscriptionManager = getSystemService<SubscriptionManager>()!!

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(false) }
            var simCards by remember { mutableStateOf<List<SimCardInfo>>(emptyList()) }

            // 获取SIM卡信息
            LaunchedEffect(Unit) {
                simCards = getSimCardInfoList()
                // 只要有任意一张SIM卡Ready，就允许PASS
                passEnabled = simCards.any { it.isReady }
            }

            TestItemScreen(
                testName = testName,
                testDescription = "请检查SIM卡是否正常识别\n\n" +
                        "操作步骤：\n" +
                        "1. 确认SIM卡已插入\n" +
                        "2. 检查信号强度\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                passEnabled = passEnabled,
                content = {
                    // 自定义内容：SIM卡信息列表
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (simCards.isEmpty()) {
                            Text(
                                text = "未检测到SIM卡",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        } else {
                            simCards.forEach { simInfo ->
                                SimCardInfoRow(simInfo = simInfo)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            )

            // 倒计时逻辑 - 超时直接finish，不弹框
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束，标记为FAIL
                saveAndFinish(false)
            }
        }
    }

    // 获取所有SIM卡信息列表
    private fun getSimCardInfoList(): List<SimCardInfo> {
        val simList = mutableListOf<SimCardInfo>()

        try {
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
            if (activeSubscriptionInfoList != null) {
                for (subscriptionInfo in activeSubscriptionInfoList) {
                    val slotIndex = subscriptionInfo.simSlotIndex
                    val displayName = subscriptionInfo.displayName?.toString() ?: "SIM $slotIndex"
                    val carrierName = subscriptionInfo.carrierName?.toString() ?: "未知运营商"
                    val simSerialNumber = subscriptionInfo.iccId ?: "未知"

                    // 获取该卡槽的SimState来判断是否就绪
                    val simStateRaw = telephonyManager.getSimState(slotIndex)
                    val isReady = simStateRaw == 5  // SIM_STATE_READY
                    val simState = if (isReady) "正常" else "未就绪"

                    simList.add(
                        SimCardInfo(
                            slotIndex = slotIndex,
                            displayName = displayName,
                            carrierName = carrierName,
                            simSerialNumber = simSerialNumber,
                            isReady = isReady,
                            simState = simState
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 如果没有获取到任何SIM卡信息，尝试使用传统方式检测
        if (simList.isEmpty()) {
            for (slot in 0 until telephonyManager.phoneCount) {
                val simStateRaw = telephonyManager.getSimState(slot)
                val stateStr = when (simStateRaw) {
                    5 -> "正常"  // SIM_STATE_READY
                    1 -> "不存在"  // SIM_STATE_ABSENT
                    2, 3 -> "锁定"  // SIM_STATE_LOCKED_PIN or SIM_STATE_LOCKED_PUK
                    else -> "未知"
                }
                val isReady = simStateRaw == 5  // SIM_STATE_READY
                simList.add(
                    SimCardInfo(
                        slotIndex = slot,
                        displayName = "SIM ${slot + 1}",
                        carrierName = if (isReady) "已识别" else "-",
                        simSerialNumber = "-",
                        isReady = isReady,
                        simState = stateStr
                    )
                )
            }
        }

        return simList
    }

    @Composable
    private fun SimCardInfoRow(simInfo: SimCardInfo) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (simInfo.isReady) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFB71C1C).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = simInfo.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (simInfo.isReady) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                    Text(
                        text = simInfo.simState,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (simInfo.isReady) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "运营商", value = simInfo.carrierName)
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow(label = "卡槽", value = "${simInfo.slotIndex + 1}")
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow(label = "ICCID", value = simInfo.simSerialNumber)
            }
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
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

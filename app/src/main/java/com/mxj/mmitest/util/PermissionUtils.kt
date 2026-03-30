package com.mxj.mmitest.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * 权限管理工具类
 * 提供统一的权限申请和检查接口
 */
object PermissionUtils {

    /**
     * 权限分组 - 运行时权限
     */
    object RuntimePermissions {
        // 电话相关
        val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        val READ_PHONE_NUMBERS = Manifest.permission.READ_PHONE_NUMBERS
        val CALL_PHONE = Manifest.permission.CALL_PHONE

        // 存储相关
        val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

        // 位置相关（GPS、WiFi扫描）
        val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        // 相机
        val CAMERA = Manifest.permission.CAMERA

        // 音频
        val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

        // 蓝牙
        val BLUETOOTH = Manifest.permission.BLUETOOTH
        val BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN
        val BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT
        val BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN

        // WiFi
        val ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE
        val CHANGE_WIFI_STATE = Manifest.permission.CHANGE_WIFI_STATE
        val ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE

        // FM收音机
        val RECEIVE_BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED
    }

    /**
     * 根据测试项ID获取所需权限列表
     */
    fun getPermissionsForTestItem(testItemId: Int): List<String> {
        return when (testItemId) {
            1 -> listOf(RuntimePermissions.READ_PHONE_STATE) // SIM卡测试
            2 -> listOf( // 存储测试
                RuntimePermissions.READ_EXTERNAL_STORAGE,
                RuntimePermissions.WRITE_EXTERNAL_STORAGE
            )
            9 -> listOf(RuntimePermissions.READ_PHONE_STATE) // 充电测试
            12 -> listOf(RuntimePermissions.RECORD_AUDIO) // 音频回环测试
            16, 17 -> listOf(RuntimePermissions.CAMERA) // 摄像头测试
            18 -> listOf( // 电话测试
                RuntimePermissions.READ_PHONE_STATE,
                RuntimePermissions.CALL_PHONE
            )
            19 -> listOf( // WiFi测试
                RuntimePermissions.ACCESS_WIFI_STATE,
                RuntimePermissions.CHANGE_WIFI_STATE,
                RuntimePermissions.ACCESS_NETWORK_STATE
            )
            20 -> getBluetoothPermissions() // 蓝牙测试
            21 -> listOf( // GPS测试
                RuntimePermissions.ACCESS_FINE_LOCATION,
                RuntimePermissions.ACCESS_COARSE_LOCATION
            )
            else -> emptyList()
        }
    }

    /**
     * 获取蓝牙权限列表（根据Android版本不同）
     */
    private fun getBluetoothPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                RuntimePermissions.BLUETOOTH,
                RuntimePermissions.BLUETOOTH_ADMIN,
                RuntimePermissions.BLUETOOTH_CONNECT,
                RuntimePermissions.BLUETOOTH_SCAN
            )
        } else {
            listOf(
                RuntimePermissions.BLUETOOTH,
                RuntimePermissions.BLUETOOTH_ADMIN
            )
        }
    }

    /**
     * 检查是否具有所有指定权限
     */
    fun hasAllPermissions(context: Context, permissions: List<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否具有任何指定权限
     */
    fun hasAnyPermission(context: Context, permissions: List<String>): Boolean {
        return permissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 获取尚未授予的权限列表
     */
    fun getMissingPermissions(context: Context, permissions: List<String>): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 获取权限的友好名称
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            RuntimePermissions.READ_PHONE_STATE -> "读取手机状态"
            RuntimePermissions.READ_PHONE_NUMBERS -> "读取手机号码"
            RuntimePermissions.CALL_PHONE -> "拨打电话"
            RuntimePermissions.READ_EXTERNAL_STORAGE -> "读取外部存储"
            RuntimePermissions.WRITE_EXTERNAL_STORAGE -> "写入外部存储"
            RuntimePermissions.ACCESS_FINE_LOCATION -> "精确位置"
            RuntimePermissions.ACCESS_COARSE_LOCATION -> "粗略位置"
            RuntimePermissions.CAMERA -> "相机"
            RuntimePermissions.RECORD_AUDIO -> "录音"
            RuntimePermissions.BLUETOOTH -> "蓝牙"
            RuntimePermissions.BLUETOOTH_ADMIN -> "蓝牙管理"
            RuntimePermissions.BLUETOOTH_CONNECT -> "蓝牙连接"
            RuntimePermissions.BLUETOOTH_SCAN -> "蓝牙扫描"
            RuntimePermissions.ACCESS_WIFI_STATE -> "WiFi状态"
            RuntimePermissions.CHANGE_WIFI_STATE -> "改变WiFi状态"
            RuntimePermissions.ACCESS_NETWORK_STATE -> "网络状态"
            RuntimePermissions.RECEIVE_BOOT_COMPLETED -> "开机广播"
            else -> permission
        }
    }

    /**
     * 获取所有必要权限的友好名称列表
     */
    fun getAllRequiredPermissionsDisplayNames(testItemId: Int): List<String> {
        return getPermissionsForTestItem(testItemId).map { getPermissionDisplayName(it) }
    }
}

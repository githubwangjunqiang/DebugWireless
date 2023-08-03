package com.gx.cash.fdfdfd

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PermissionResult

/**
 * Created by Android-小强 on 2023/8/2.
 * mailbox:980766134@qq.com
 * description:
 */

const val TAG = "12345"

object WiflUtils {
    // 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    enum class WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }


    @RequiresPermission(
        allOf = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
        )
    )
    fun startWifl(context: Context, activity: Activity) {
        //检测无线是否开启

        if (!checkWifiIsEnable(context)) {
            Log.d(TAG, "startWifl:无线没有开启 ")
            openWiflSetting(activity)
            return
        }


        //如果已经开启 就刷新无线列表

        //扫描

        scanWifl(context) { success ->
            Log.d(TAG, "startWifl: 扫描回调广播回调：$success")
            if (success) {
                //创建链接 链接指定的ssid
                loadCacheList(context)
                openWiflSSID(context)
            } else {
                loadCacheList(context)
            }
        }


    }

    private fun scanWifl(context: Context, callback: (success: Boolean) -> Unit): Boolean {

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                callback.invoke(success)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(wifiScanReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(wifiScanReceiver, intentFilter)
        }

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            callback.invoke(false)
            return false
        }
        return true
    }

    fun loadCacheList(context: Context) {
        val wifiManager =
            context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "loadCacheList: 没有权限-ACCESS_FINE_LOCATION")
            return
        }
        val scanResults = wifiManager.scanResults
        scanResults.forEach { data ->
            Log.d(TAG, "loadCacheList-BSSID: ${data.BSSID}")
            Log.d(TAG, "loadCacheList-SSID: ${data.SSID}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "loadCacheList-wifiSsid: ${data.wifiSsid}")
            }

        }
    }

    private fun openWiflSSID(context: Context) {
        try {
            val wifiManager =
                context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager


            val ssid_ApName = "Xiaomi_qiang"
            val passwored = "15075818555"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val suggestion2 = WifiNetworkSuggestion.Builder()
                    .setSsid(ssid_ApName)
                    .setWpa2Passphrase(passwored)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val networkSuggestions = wifiManager.networkSuggestions
                    if (!networkSuggestions.isNullOrEmpty()) {
                        networkSuggestions.forEach { data ->
                            Log.d(TAG, "forEach-ssid: ${data.ssid}")
                            Log.d(TAG, "forEach-isHiddenSsid: ${data.isHiddenSsid}")
                            Log.d(TAG, "forEach-isEnhancedOpen: ${data.isEnhancedOpen}")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Log.d(TAG, "forEach-wifiSsid: ${data.wifiSsid}")
                            }
                            if (data.ssid == ssid_ApName) {
                                wifiManager.removeNetworkSuggestions(listOf(data))
                            }

                        }
                    }
                }
                val status = wifiManager.addNetworkSuggestions(listOf(suggestion2))
                if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                    // do error handling here
                    Log.d(TAG, "openWiflSSID: 网络建议失败")
                    return
                }

                // Optional (Wait for post connection broadcast to one of your suggestions)
                val intentFilter =
                    IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

                val broadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                            return;
                        }
                        Log.d(TAG, "onReceive: 已经链接到指定网络")
                        // do post connect processing here
                        context.unregisterReceiver(this)
                    }
                };
                context.registerReceiver(broadcastReceiver, intentFilter);
            } else {
                val createWifiInfo =
                    createWifiInfo(ssid_ApName, passwored, WifiCipherType.WIFICIPHER_WPA)
                val addNetwork = wifiManager.addNetwork(createWifiInfo)

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 检查wifi是否可用
     */
    private fun checkWifiIsEnable(context: Context): Boolean {
        try {
            val wifiManager =
                context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
            return null != wifiManager && wifiManager.isWifiEnabled
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun openWiflSetting(context: Activity): Boolean {
        return try {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun refreshTheWirelessList(context: Context): Boolean {
        try {
            val wifiManager =
                context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.startScan()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun createWifiInfo(ssID: String, passWord: String, type: WifiCipherType): WifiConfiguration {
        Log.d(TAG, "createWifiInfo：" + ssID + "，" + passWord + "，" + type);
        val config = WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssID + "\"";

        // NOPASS
        if (type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // WEP
//        if (type == WifiCipherType.WIFICIPHER_WEP) {
//            if (!TextUtils.isEmpty(passWord)) {
//                if (isHexWepKey(passWord)) {
//                    config.wepKeys[0] = passWord;
//                } else {
//                    config.wepKeys[0] = "\"" + passWord + "\"";
//                }
//            }
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
//        }
        // WPA
        if (type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + passWord + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

            // 此处保证网络自动重联
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

//    private fun isHexWepKey(wepKey: String): Boolean {
//        Log.d(TAG, "isHexWepKey：$wepKey")
//        val len = wepKey.length
//
//        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
//        return if (len != 10 && len != 26 && len != 58) {
//            false
//        } else
//            isHex(wepKey)
//    }
//
//    private fun isHex(key: String): Boolean {
//        Log.d(TAG, "isHex：$key")
//        for (i in key.length - 1 downTo 0) {
//            val c = key[i]
//            if (((c in '0'..'9' || c >= 'A' && c <= 'F' || c >= 'a') && c) > 'f') {
//                return false
//            }
//        }
//        return true
//    }
}
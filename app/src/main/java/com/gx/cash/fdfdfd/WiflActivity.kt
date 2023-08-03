package com.gx.cash.fdfdfd

import android.Manifest
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.Result
import com.google.zxing.client.android.CaptureActivity
import com.google.zxing.client.android.wifi.WifiConfigManager
import com.google.zxing.client.result.WifiParsedResult
import com.google.zxing.client.result.WifiResultParser

/**
 * Created by Android-小强 on 2023/8/2.
 * mailbox:980766134@qq.com
 * description:
 */
class WiflActivity : AppCompatActivity() {
    lateinit var tvTitle: TextView
    lateinit var btn: TextView
    lateinit var google_code: TextView
    lateinit var google_code2: TextView
    lateinit var wiflw: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        tvTitle = findViewById<TextView>(R.id.tv_title)
        btn = findViewById<TextView>(R.id.btn)
        google_code = findViewById<TextView>(R.id.google_code)
        google_code2 = findViewById<TextView>(R.id.google_code2)
        wiflw = findViewById<TextView>(R.id.wiflw)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            ), 200025
        )



        btn.setOnClickListener {
            startActivity(Intent(this, CaptureActivity::class.java))
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200025
//                )
//                return@setOnClickListener
//            }
//            WiflUtils.startWifl(applicationContext, this)
        }


        google_code.setOnClickListener {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC
                )
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(this, options)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    // Task completed successfully
                    val rawValue: String? = barcode.rawValue
                    Log.d(TAG, "addOnSuccessListener:扫码成功：$rawValue ")
                    tvTitle.text = "扫码结果是：$rawValue"
                }
                .addOnCanceledListener {
                    // Task canceled
                    Log.d(TAG, "addOnCanceledListener: 扫码取消")
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    e.printStackTrace()
                    Log.d(TAG, "addOnFailureListener: 扫码失败:${e.printStackTrace()}")
                }
        }
        google_code2.setOnClickListener {
            startActivity(Intent(this, CamXActivity::class.java))

        }

        wiflw.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ), 200026
            )
            // WifiManager wifiManager =
            //          (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            var wmanager: WifiManager =
                applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
            val parseResult = WifiResultParser.parseResult(
                Result(
                    "WIFI:T:WPA;P:15075818555;S:Xiaomi_qiang;H:false;",
                    null,
                    null,
                    null
                )
            )
            WifiConfigManager(wmanager).execute(parseResult as WifiParsedResult)

        }
    }


}

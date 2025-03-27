package com.example.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListView: ListView
    private lateinit var wifiDetailsTextView: TextView

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
            if (success) {
                displayWifiNetworks()
            } else {
                Toast.makeText(this@MainActivity, "WiFi Scan failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiListView = findViewById(R.id.wifiListView)
        wifiDetailsTextView = findViewById(R.id.wifiDetailsTextView)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            scanWifiNetworks()
        }

        wifiListView.setOnItemClickListener { _, _, position, _ ->
            val wifiInfo = wifiManager.connectionInfo
            val selectedSSID = wifiListView.adapter.getItem(position) as String

            val details = """
                SSID: $selectedSSID
                IP Address: ${wifiInfo.ipAddress}
                MAC Address: ${wifiInfo.macAddress}
            """.trimIndent()

            wifiDetailsTextView.text = details
            wifiDetailsTextView.setTextColor(Color.RED) // ✅ WiFi details text in red
        }
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            scanWifiNetworks()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scanWifiNetworks() {
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
        val success = wifiManager.startScan()
        if (!success) {
            Toast.makeText(this, "WiFi Scan initiation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayWifiNetworks() {
        val results: List<ScanResult> = wifiManager.scanResults
        val wifiNames = results.map { it.SSID }.filter { it.isNotEmpty() }

        val adapter = WifiListAdapter(this, wifiNames) // ✅ Use custom adapter
        wifiListView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }
}

// ✅ Custom Adapter (Same Background, Blue Text for WiFi List Items)
class WifiListAdapter(context: Context, wifiList: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, wifiList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (view is TextView) {
            view.setTextColor(Color.BLUE) // ✅ Keep WiFi list text blue
            view.textSize = 18f
        }
        return view
    }
}

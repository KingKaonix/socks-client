package com.example.socksclient

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var hostInput: EditText
    private lateinit var portInput: EditText
    private var autoConnect = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "SocksClient"
            textSize = 20f
        }
        hostInput = EditText(this).apply {
            hint = "Host IP (auto: 192.168.49.1)"
            setText("192.168.49.1")
        }
        portInput = EditText(this).apply {
            hint = "Port"
            setText("1080")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val connectBtn = Button(this).apply { text = "Connect" }
        val disconnectBtn = Button(this).apply { text = "Disconnect" }
        statusText = TextView(this).apply { text = "Status: Disconnected" }

        layout.addView(title)
        layout.addView(hostInput)
        layout.addView(portInput)
        layout.addView(connectBtn)
        layout.addView(disconnectBtn)
        layout.addView(statusText)
        setContentView(layout)

        connectBtn.setOnClickListener { connect() }

        disconnectBtn.setOnClickListener {
            stopService(Intent(this, SocksVpnService::class.java))
            statusText.text = "Status: Disconnected"
        }

        // Auto-connect on launch
        if (autoConnect) {
            connect()
        }
    }

    private fun connect() {
        val host = hostInput.text.toString().ifEmpty { "192.168.49.1" }
        val port = portInput.text.toString().toIntOrNull() ?: 1080
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 0)
        } else {
            startVpn(host, port)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val host = hostInput.text.toString().ifEmpty { "192.168.49.1" }
            val port = portInput.text.toString().toIntOrNull() ?: 1080
            startVpn(host, port)
        }
    }

    private fun startVpn(host: String, port: Int) {
        val intent = Intent(this, SocksVpnService::class.java).apply {
            putExtra("host", host)
            putExtra("port", port)
        }
        startForegroundService(intent)
        statusText.text = "Status: Connected to $host:$port"
    }
}

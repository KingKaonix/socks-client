package com.example.socksclient

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import android.widget.LinearLayout
import android.view.Gravity

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var hostInput: EditText
    private lateinit var portInput: EditText

    private val bgColor = Color.parseColor("#0F0F23")
    private val surfaceColor = Color.parseColor("#1A1A2E")
    private val primaryColor = Color.parseColor("#00B894")
    private val accentColor = Color.parseColor("#6C5CE7")
    private val textColor = Color.parseColor("#FFFFFF")
    private val textSecondary = Color.parseColor("#A0A0B8")
    private val successColor = Color.parseColor("#00B894")
    private val errorColor = Color.parseColor("#FF6B6B")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply { setBackgroundColor(bgColor) }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 32)
        }
        val iconView = TextView(this).apply { text = "🔗"; textSize = 36f }
        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 0, 0, 0)
        }
        val title = TextView(this).apply {
            text = "SocksClient"
            textSize = 28f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Zero-Config VPN Client"
            textSize = 14f
            setTextColor(textSecondary)
        }
        titleCol.addView(title)
        titleCol.addView(subtitle)
        headerLayout.addView(iconView)
        headerLayout.addView(titleCol)
        layout.addView(headerLayout)

        // Host Card
        val hostCard = createCard()
        val hostLabel = TextView(this).apply {
            text = "HOST ADDRESS"
            textSize = 11f
            setTextColor(textSecondary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 8)
        }
        hostInput = EditText(this).apply {
            hint = "192.168.49.1"
            setText("192.168.49.1")
            textSize = 18f
            setTextColor(textColor)
            setHintTextColor(textSecondary)
            setBackgroundColor(surfaceColor)
            setPadding(24, 16, 24, 16)
        }
        hostCard.addView(hostLabel)
        hostCard.addView(hostInput)
        layout.addView(hostCard)

        // Port Card
        val portCard = createCard()
        val portLabel = TextView(this).apply {
            text = "PORT"
            textSize = 11f
            setTextColor(textSecondary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 8)
        }
        portInput = EditText(this).apply {
            hint = "1080"
            setText("1080")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            textSize = 18f
            setTextColor(textColor)
            setHintTextColor(textSecondary)
            setBackgroundColor(surfaceColor)
            setPadding(24, 16, 24, 16)
        }
        portCard.addView(portLabel)
        portCard.addView(portInput)
        layout.addView(portCard)

        // Status Card
        val statusCard = createCard()
        val statusLabel = TextView(this).apply {
            text = "STATUS"
            textSize = 11f
            setTextColor(textSecondary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 8)
        }
        statusText = TextView(this).apply {
            text = "Disconnected"
            textSize = 20f
            setTextColor(errorColor)
            typeface = Typeface.DEFAULT_BOLD
        }
        statusCard.addView(statusLabel)
        statusCard.addView(statusText)
        layout.addView(statusCard)

        // Buttons
        val btnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }
        val connectBtn = createButton("⚡ Connect", primaryColor)
        val disconnectBtn = createButton("■ Disconnect", errorColor)
        btnLayout.addView(connectBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, 8, 0) })
        btnLayout.addView(disconnectBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(8, 0, 0, 0) })
        layout.addView(btnLayout)

        // Info Card
        val infoCard = createCard()
        val infoText = TextView(this).apply {
            text = "All traffic will be routed through the SOCKS5 proxy on the host device. Games, browsers, and all apps work automatically."
            textSize = 12f
            setTextColor(textSecondary)
        }
        infoCard.addView(infoText)
        layout.addView(infoCard)

        scroll.addView(layout)
        setContentView(scroll)

        connectBtn.setOnClickListener { connect() }
        disconnectBtn.setOnClickListener {
            stopService(Intent(this, SocksVpnService::class.java))
            statusText.text = "Disconnected"
            statusText.setTextColor(errorColor)
        }

        // Auto-connect on launch
        connect()
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
        statusText.text = "Connected to $host:$port"
        statusText.setTextColor(successColor)
    }

    private fun createCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(surfaceColor)
            setPadding(32, 24, 32, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }
    }

    private fun createButton(text: String, color: Int): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setPadding(16, 12, 16, 12)
        }
    }
}

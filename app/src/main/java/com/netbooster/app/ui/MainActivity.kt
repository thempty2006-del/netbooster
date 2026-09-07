package com.netbooster.app.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netbooster.app.core.MtuProber
import com.netbooster.app.core.RootTuner
import com.netbooster.app.core.TunnelVpnService
import com.netbooster.app.databinding.ActivityMainBinding
import com.netbooster.app.util.Prefs
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var connected = false
    private lateinit var prefs: Prefs

    private val vpnPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) startVpn()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        binding.toggleButton.setOnClickListener {
            if (connected) stopVpn() else requestVpnPermission()
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, ServerConfigActivity::class.java))
        }

        binding.probeMtuButton.setOnClickListener { probeMtu() }

        checkRoot()
    }

    private fun checkRoot() {
        thread {
            val rooted = RootTuner.isRooted()
            runOnUiThread {
                if (rooted) {
                    binding.rootStatusText.text = "Root detected — real TCP tuning (BBR, buffers) available"
                    if (prefs.rootTuningEnabled) RootTuner.applyNetworkTuning()
                } else {
                    binding.rootStatusText.text = "No root — DNS optimization + MTU advice only"
                }
            }
        }
    }

    private fun probeMtu() {
        binding.probeMtuButton.isEnabled = false
        binding.probeMtuButton.text = "Probing..."
        thread {
            val mtu = MtuProber.probe()
            runOnUiThread {
                binding.probeMtuButton.isEnabled = true
                binding.probeMtuButton.text = "Probe optimal MTU"
                if (mtu != null) {
                    prefs.mtu = mtu
                    Toast.makeText(this, "Recommended MTU: $mtu (saved)", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Probe failed — network may block ping", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpn()
        }
    }

    private fun startVpn() {
        startService(Intent(this, TunnelVpnService::class.java).setAction(TunnelVpnService.ACTION_CONNECT))
        connected = true
        binding.statusText.text = "Connected"
        binding.toggleButton.text = "Disconnect"
    }

    private fun stopVpn() {
        startService(Intent(this, TunnelVpnService::class.java).setAction(TunnelVpnService.ACTION_DISCONNECT))
        connected = false
        binding.statusText.text = "Disconnected"
        binding.toggleButton.text = "Connect"
    }
}

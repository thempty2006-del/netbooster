package com.netbooster.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netbooster.app.databinding.ActivityServerConfigBinding
import com.netbooster.app.util.Prefs

class ServerConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerConfigBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        binding.dnsPrimaryInput.setText(prefs.dnsPrimary)
        binding.dnsSecondaryInput.setText(prefs.dnsSecondary)
        binding.mtuInput.setText(prefs.mtu.toString())
        binding.rootTuningCheckbox.isChecked = prefs.rootTuningEnabled

        binding.saveButton.setOnClickListener {
            prefs.dnsPrimary = binding.dnsPrimaryInput.text.toString().ifBlank { "1.1.1.1" }
            prefs.dnsSecondary = binding.dnsSecondaryInput.text.toString().ifBlank { "1.0.0.1" }
            prefs.mtu = binding.mtuInput.text.toString().toIntOrNull() ?: 1420
            prefs.rootTuningEnabled = binding.rootTuningCheckbox.isChecked
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

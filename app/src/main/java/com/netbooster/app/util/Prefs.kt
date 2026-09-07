package com.netbooster.app.util

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("netbooster_prefs", Context.MODE_PRIVATE)

    var dnsPrimary: String
        get() = sp.getString("dns_primary", "1.1.1.1") ?: "1.1.1.1"
        set(v) = sp.edit().putString("dns_primary", v).apply()

    var dnsSecondary: String
        get() = sp.getString("dns_secondary", "1.0.0.1") ?: "1.0.0.1"
        set(v) = sp.edit().putString("dns_secondary", v).apply()

    var mtu: Int
        get() = sp.getInt("mtu", 1420)
        set(v) = sp.edit().putInt("mtu", v).apply()

    var useDoH: Boolean
        get() = sp.getBoolean("use_doh", true)
        set(v) = sp.edit().putBoolean("use_doh", v).apply()

    var rootTuningEnabled: Boolean
        get() = sp.getBoolean("root_tuning_enabled", false)
        set(v) = sp.edit().putBoolean("root_tuning_enabled", v).apply()
}

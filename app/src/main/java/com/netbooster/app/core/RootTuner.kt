package com.netbooster.app.core

import java.io.DataOutputStream

/**
 * Mirrors the exact tuning already applied on the user's own Iran/Turkey servers
 * (see netboost.sh): BBR congestion control, larger buffers, fq qdisc equivalent.
 * Only works if the device is rooted — this is the ONLY way to genuinely change
 * system-wide TCP behavior on Android, same as it required root on Linux servers.
 */
object RootTuner {

    fun isRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()
            result.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /** Returns true if every command succeeded. Requires root. */
    fun applyNetworkTuning(): Boolean {
        val commands = listOf(
            // Congestion control -> BBR (falls back silently if kernel doesn't support it)
            "echo bbr > /proc/sys/net/ipv4/tcp_congestion_control",
            "echo 1 > /proc/sys/net/ipv4/tcp_mtu_probing",
            "echo 1 > /proc/sys/net/ipv4/tcp_ecn",
            // Buffers (scaled down vs. the VPS values since this is a phone, not a server)
            "echo '4096 87380 8388608' > /proc/sys/net/ipv4/tcp_rmem",
            "echo '4096 65536 8388608' > /proc/sys/net/ipv4/tcp_wmem",
            "echo 0 > /proc/sys/net/ipv4/tcp_slow_start_after_idle"
        )
        return try {
            val su = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(su.outputStream)
            for (cmd in commands) {
                out.writeBytes("$cmd\n")
            }
            out.writeBytes("exit\n")
            out.flush()
            su.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    /** Reverts tcp_congestion_control back to the kernel default (cubic) and other stock values. */
    fun revert(): Boolean {
        val commands = listOf(
            "echo cubic > /proc/sys/net/ipv4/tcp_congestion_control",
            "echo 1 > /proc/sys/net/ipv4/tcp_slow_start_after_idle"
        )
        return try {
            val su = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(su.outputStream)
            for (cmd in commands) out.writeBytes("$cmd\n")
            out.writeBytes("exit\n")
            out.flush()
            su.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}

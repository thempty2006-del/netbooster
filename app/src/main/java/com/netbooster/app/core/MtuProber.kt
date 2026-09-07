package com.netbooster.app.core

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Binary-searches for the largest ICMP payload that goes through without fragmentation,
 * against the current network. Doesn't change anything by itself — just tells the user
 * (or RootTuner, if rooted) what MTU to use.
 */
object MtuProber {

    private const val ICMP_HEADER_OVERHEAD = 28 // 20 IP + 8 ICMP

    /** Returns the recommended MTU, or null if probing failed (e.g. ping blocked). */
    fun probe(host: String = "1.1.1.1"): Int? {
        var low = 1200
        var high = 1500
        var best: Int? = null

        while (low <= high) {
            val mid = (low + high) / 2
            val payload = mid - ICMP_HEADER_OVERHEAD
            if (pingWithSize(host, payload)) {
                best = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best
    }

    private fun pingWithSize(host: String, payloadSize: Int): Boolean {
        return try {
            // -M do  => require "don't fragment", so we learn the true path MTU
            // -c 1   => single probe, -W 2 => 2s timeout
            val process = ProcessBuilder(
                "ping", "-M", "do", "-c", "1", "-W", "2", "-s", payloadSize.toString(), host
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output.contains("1 received") || output.contains("1 packets received")
        } catch (e: Exception) {
            false
        }
    }
}

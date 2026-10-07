package com.shilapi.xcertplay.network

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

/** Keeps Wi-Fi in high-performance mode during an active CarPlay media session. */
internal class WifiPerformanceLock(context: Context) : AutoCloseable {
    private val lock: WifiManager.WifiLock? = runCatching {
        val manager = context.applicationContext.getSystemService(WifiManager::class.java)
        if (manager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return@runCatching null
        manager.createWifiLock(
            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            "DiPlay-CarPlay-Video",
        ).apply { setReferenceCounted(false) }
    }.getOrNull()

    @Synchronized
    fun acquire() {
        if (lock?.isHeld == false) runCatching { lock.acquire() }
    }

    @Synchronized
    override fun close() {
        runCatching {
            if (lock?.isHeld == true) lock.release()
        }
    }
}

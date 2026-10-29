package com.example.pingpong.api

import com.example.pingpong.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.request.*
import java.util.concurrent.ConcurrentHashMap

class ResponseCache<T : Any>(private val ttlMillis: Long = AppConfig.CACHE_TTL_SECONDS * 1000) {

    private data class Entry<T>(val value: T, val storedAt: Long)

    private val entries = ConcurrentHashMap<String, Entry<T>>()

    suspend fun getOrPut(call: ApplicationCall, loader: suspend () -> T): T {
        val key = call.request.uri
        val now = System.currentTimeMillis()
        entries[key]?.takeIf { now - it.storedAt < ttlMillis }?.let { return it.value }

        val value = loader()
        entries[key] = Entry(value, now)
        return value
    }
}

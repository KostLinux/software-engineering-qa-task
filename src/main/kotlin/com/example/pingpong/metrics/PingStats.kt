package com.example.pingpong.metrics

import com.example.pingpong.config.AppConfig
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentLinkedDeque

class PingSample(val startedAt: Long, val durationNanos: Long)

@Serializable
data class PingStatsSnapshot(
    val sampleCount: Int,
    val avgLatencyMicros: Double,
    val p99LatencyMicros: Double,
)

object PingStats {
    private const val WINDOW_MILLIS = AppConfig.PING_STATS_WINDOW_SECONDS * 1000

    private val samples = ConcurrentLinkedDeque<PingSample>()

    fun record(sample: PingSample) {
        samples.addLast(sample)
        evictExpired()
    }

    fun snapshot(): PingStatsSnapshot {
        evictExpired()
        val latencies = samples.map { it.durationNanos / 1_000.0 }.sorted()
        if (latencies.isEmpty()) return PingStatsSnapshot(0, 0.0, 0.0)
        return PingStatsSnapshot(
            sampleCount = latencies.size,
            avgLatencyMicros = latencies.average(),
            p99LatencyMicros = latencies[((latencies.size - 1) * 0.99).toInt()],
        )
    }

    private fun evictExpired() {
        val cutoff = System.currentTimeMillis() - WINDOW_MILLIS
        while (samples.peekFirst()?.let { it.startedAt < cutoff } == true) {
            samples.pollFirst()
        }
    }
}

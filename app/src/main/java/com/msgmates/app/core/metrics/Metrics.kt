package com.msgmates.app.core.metrics

interface Metrics {
    fun increment(name: String, tags: Map<String, String> = emptyMap())
    fun recordTime(name: String, durationMs: Long, tags: Map<String, String> = emptyMap())
    fun setGauge(name: String, value: Number, tags: Map<String, String> = emptyMap())
}

class NoopMetrics @javax.inject.Inject constructor() : Metrics {
    override fun increment(name: String, tags: Map<String, String>) {}
    override fun recordTime(name: String, durationMs: Long, tags: Map<String, String>) {}
    override fun setGauge(name: String, value: Number, tags: Map<String, String>) {}
}

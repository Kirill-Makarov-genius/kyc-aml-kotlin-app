package com.study.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig

val appMicrometerRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
    config().commonTags("application", "kyc-aml-app")}

fun Application.configureMonitoring() {
    install(MicrometerMetrics){
        registry = appMicrometerRegistry
        timers { call, throwable ->
            tag("application", "kyc-aml-app")
        }

        // Set histograms to see p50, p90, p95, p99
        distributionStatisticConfig = distributionStatisticConfig.merge(
            DistributionStatisticConfig.builder()
                .percentiles(0.5, 0.9, 0.95, 0.99)
                .build()
        )
        // Set basics metrics for app
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            JvmInfoMetrics(),
            FileDescriptorMetrics(),
            LogbackMetrics(),
            UptimeMetrics()
        )
    }
    // endpoint for Prometheus
    routing {
        get("/metrics"){
            call.respondText(appMicrometerRegistry.scrape())
        }
    }
}
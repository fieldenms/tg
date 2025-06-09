package fielden.platform.metrics;

import com.google.inject.Inject;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/// This IoC module registers all built-in meters.
///
/// [MetricsCoreIocModule] is a required dependency.
///
public final class CommonMetricsIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        requestStaticInjection(CommonMetricsIocModule.class);
    }

    @Inject
    static void registerMetrics(final MeterRegistry meterRegistry) {
        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
    }

}

package fielden.platform.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/// Core IoC module for the metrics system.
///
public final class MetricsCoreIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(MeterRegistry.class).toInstance(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
    }

}

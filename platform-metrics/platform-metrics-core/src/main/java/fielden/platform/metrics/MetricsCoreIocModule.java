package fielden.platform.metrics;

import com.google.inject.Inject;
import com.google.inject.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

import java.util.Properties;

import static org.apache.logging.log4j.LogManager.getLogger;

/// Core IoC module for the metrics system.
///
/// ### Bindings
///
/// * [#meterRegistry(MetricsConfig)]
/// * [MetricsConfig] -- configuration is read from application properties.
///
public final class MetricsCoreIocModule extends AbstractPlatformIocModule {

    private static final Logger LOGGER = getLogger();

    private final Properties properties;

    public MetricsCoreIocModule(final Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        requestStaticInjection(MetricsCoreIocModule.class);

        bind(MetricsConfig.class).toInstance(MetricsConfig.fromProperties(properties));
    }

    /// If metrics are enabled, binds a [PrometheusMeterRegistry].
    /// Otherwise, binds a noop registry.
    ///
    @Provides
    MeterRegistry meterRegistry(final MetricsConfig config) {
        return switch (config.mode()) {
            case ENABLED -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            case DISABLED -> {
                final var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                registry.close();
                yield registry;
            }
        };
    }

    @Inject
    static void logMetricsConfig(final MetricsConfig metricsConfig) {
        LOGGER.info("Metrics configuration: %s".formatted(metricsConfig));
    }

}

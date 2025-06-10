package fielden.platform.metrics;

import com.google.inject.Inject;
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
/// * [MeterRegistry].
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

        bind(MeterRegistry.class).toInstance(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
        bind(MetricsConfig.class).toInstance(MetricsConfig.fromProperties(properties));
    }

    @Inject
    static void logMetricsConfig(final MetricsConfig metricsConfig) {
        LOGGER.info("Metrics configuration: %s".formatted(metricsConfig));
    }

}

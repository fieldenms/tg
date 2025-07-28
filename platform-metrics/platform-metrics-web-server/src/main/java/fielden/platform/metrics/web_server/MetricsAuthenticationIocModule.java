package fielden.platform.metrics.web_server;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import fielden.platform.metrics.MetricsConfig;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

import java.util.Optional;

/// Configures authentication for the metrics web resource.
///
/// Applications that use [MetricsResourceFactory] must install this module.
///
/// ### Bindings
///
/// * [#apiKey(MetricsConfig)]
///
public class MetricsAuthenticationIocModule extends AbstractPlatformIocModule {

    /// Name of a binding for the API key.
    ///
    public static final String TG_METRICS_API_KEY = "tg.metrics.apiKey";

    /// If metrics are enabled, an API key must be specified as a System property or an environment variable.
    /// Otherwise, an empty optional will be bound.
    ///
    @Provides
    @Named(TG_METRICS_API_KEY)
    @Singleton
    Optional<String> apiKey(final MetricsConfig config) {
        return switch (config.mode()) {
            case DISABLED -> Optional.empty();
            case ENABLED -> {
                final var value = System.getProperty(TG_METRICS_API_KEY, System.getenv(TG_METRICS_API_KEY));
                if (value == null) {
                    throw new ApplicationConfigurationException("%s is missing.".formatted(TG_METRICS_API_KEY));
                }
                yield Optional.of(value);
            }
        };
    }

}

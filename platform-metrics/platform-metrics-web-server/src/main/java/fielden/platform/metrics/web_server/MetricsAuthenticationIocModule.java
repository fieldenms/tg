package fielden.platform.metrics.web_server;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/// Configures authentication for the metrics web resource.
///
/// Applications that use [MetricsResourceFactory] must install this module.
///
/// ### Bindings
///
/// * [#TG_METRICS_API_KEY] -- must be specified as a System property or an environment variable.
///
public class MetricsAuthenticationIocModule extends AbstractPlatformIocModule {

    /// Name of a binding for the API key.
    ///
    public static final String TG_METRICS_API_KEY = "tg.metrics.apiKey";

    @Provides
    @Named(TG_METRICS_API_KEY)
    String apiKey() {
        final var value = System.getProperty(TG_METRICS_API_KEY, System.getenv(TG_METRICS_API_KEY));
        if (value == null) {
            throw new ApplicationConfigurationException("%s is missing.".formatted(TG_METRICS_API_KEY));
        }
        return value;
    }

}

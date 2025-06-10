package fielden.platform.metrics.web_server;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.security.Authenticator;

import javax.annotation.Nullable;
import java.util.Optional;

import static fielden.platform.metrics.web_server.MetricsAuthenticationIocModule.TG_METRICS_API_KEY;

/// Implements authentication for the metrics resource.
///
/// For a request to be authenticated, it must contain header [#API_KEY_HEADER] that specifies a valid [API key][MetricsAuthenticationIocModule#TG_METRICS_API_KEY].
///
/// If metrics are disabled, authentication will always fail.
///
public class MetricsAuthenticator extends Authenticator {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final Optional<String> maybeApiKey;

    @Inject
    private MetricsAuthenticator(final @Named(TG_METRICS_API_KEY) Optional<String> maybeApiKey) {
        super(null); // Context is not used by this class
        this.maybeApiKey = maybeApiKey;
    }

    @Override
    protected boolean authenticate(final Request request, final Response response) {
        return maybeApiKey
                .map(apiKey -> {
                    final @Nullable String requestApiKey = request.getHeaders().getFirstValue(API_KEY_HEADER, true);
                    return apiKey.equals(requestApiKey) || forbid(response);
                })
                .orElseGet(() -> forbid(response));
    }

    private boolean forbid(final Response response) {
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return false;
    }

}

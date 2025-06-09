package fielden.platform.metrics.web_server;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

/// Executes around a request, capturing metrics.
///
public class MetricsFilter extends Filter {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String WARN_ERROR_IN_METRICS = "Suppressed an error that occured in the metrics subsystem.";

    private final MeterRegistry meterRegistry;

    @Inject
    private MetricsFilter(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected int doHandle(final Request request, final Response response) {
        // Do not let errors that pertain to metrics inerrupt processing of the request.

        Timer.Sample sample;
        try {
            sample = Timer.start(meterRegistry);
        } catch (final Exception ex) {
            LOGGER.warn(WARN_ERROR_IN_METRICS, ex);
            sample = null;
        }

        final var result = super.doHandle(request, response);

        if (sample != null) {
            try {
                final var timer = meterRegistry.timer("http.server.request",
                                                      "path", request.getOriginalRef().getPath(),
                                                      "method", request.getMethod().getName(),
                                                      "status", String.valueOf(response.getStatus().getCode()));
                sample.stop(timer);
            } catch (final Exception ex) {
                LOGGER.warn(WARN_ERROR_IN_METRICS, ex);
            }
        }

        return result;
    }

}

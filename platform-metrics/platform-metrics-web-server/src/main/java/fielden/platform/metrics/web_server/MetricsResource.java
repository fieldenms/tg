package fielden.platform.metrics.web_server;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResource;

import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

/// Provides metrics in the OpenMetrics format.
///
/// Requires the [MeterRegistry] to be a [PrometheusMeterRegistry].
///
/// Metrics must be enabled for this class to be used.
///
public class MetricsResource extends AbstractWebResource {

    private static final String CONTENT_TYPE_OPENMETRICS = "application/openmetrics-text";
    private static final MediaType MEDIA_TYPE_OPENMETRICS = MediaType.valueOf(CONTENT_TYPE_OPENMETRICS);

    private final RestServerUtil restUtil;
    private final PrometheusMeterRegistry meterRegistry;

    MetricsResource(
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response,
            final PrometheusMeterRegistry meterRegistry,
            final RestServerUtil restUtil)
    {
        super(context, request, response, deviceProvider, dates);
        this.restUtil = restUtil;
        this.meterRegistry = meterRegistry;
    }

    @Get
    public Representation metrics() {
        return handleUndesiredExceptions(getResponse(), () -> {
            final var data = meterRegistry.scrape(CONTENT_TYPE_OPENMETRICS);
            return new StringRepresentation(data, MEDIA_TYPE_OPENMETRICS);
        }, restUtil);
    }

}

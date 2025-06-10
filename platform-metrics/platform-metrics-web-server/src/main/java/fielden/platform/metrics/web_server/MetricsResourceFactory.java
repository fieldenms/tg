package fielden.platform.metrics.web_server;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.inject.Inject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/// Factory for [MetricsResource].
///
public class MetricsResourceFactory extends Restlet {

    private final RestServerUtil restUtil;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final PrometheusMeterRegistry meterRegistry;

    @Inject
    private MetricsResourceFactory(
            final RestServerUtil restUtil,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final MeterRegistry meterRegistry)
    {
        this.restUtil = restUtil;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
        if (meterRegistry instanceof PrometheusMeterRegistry it) {
            this.meterRegistry = it;
        }
        else {
            throw new InvalidArgumentException("%s must be a %s, but was %s.".formatted(
                    MeterRegistry.class.getSimpleName(), PrometheusMeterRegistry.class.getSimpleName(), meterRegistry.getClass().getCanonicalName()));
        }
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MetricsResource(deviceProvider, dates, getContext(), request, response,
                                meterRegistry, restUtil)
                    .handle();
        }
    }

}

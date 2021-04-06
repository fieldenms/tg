package ua.com.fielden.platform.web.sse.resources;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import rx.Observable;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.sse.AbstractEventSource;
import ua.com.fielden.platform.web.sse.SseUtils;
import ua.com.fielden.platform.web.sse.exceptions.InvalidSseUriException;

/**
 * A factory for a web resource {@link EventSourcingResource} that provides a general purpose implementation for the server-side eventing
 * where events are values of the specified data stream, which is represented by an instance of {@link Observable}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EventSourcingResourceFactory extends Restlet {

    private final Injector injector;
    private final Class<? extends AbstractEventSource<?, ?>> eventSourceType;
    private final AbstractEventSource<?, ?> eventSource;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public EventSourcingResourceFactory(final Injector injector, final Class<? extends AbstractEventSource<?, ?>> eventSourceType, final IDeviceProvider deviceProvider, final IDates dates) {
        this.injector = injector;
        this.eventSourceType = eventSourceType;
        this.eventSource = null;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    public EventSourcingResourceFactory(final AbstractEventSource<?, ?> eventSource, final IDeviceProvider deviceProvider, final IDates dates) {
        this.injector = null;
        this.eventSource = eventSource;
        this.eventSourceType = null;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            if (!SseUtils.isEventSourceRequest(request)) {
                throw new InvalidSseUriException(String.format("URI [%s] is not valid for SSE.", request.getResourceRef().toString()));
            }
            if (this.eventSource != null) {
                new EventSourcingResource(eventSource, deviceProvider, dates, getContext(), request, response).handle();
            } else {
                new EventSourcingResource(injector.getInstance(eventSourceType), deviceProvider, dates, getContext(), request, response).handle();
            }
        }
    }

}

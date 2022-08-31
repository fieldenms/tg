package ua.com.fielden.platform.web.sse.resources;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.List;

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
    private final List<Class<? extends AbstractEventSource<?, ?>>> eventSourceTypes = new ArrayList<>();
    private final List<AbstractEventSource<?, ?>> eventSources = new ArrayList<>();
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public EventSourcingResourceFactory(final Injector injector, final IDeviceProvider deviceProvider, final IDates dates, final Class<? extends AbstractEventSource<?, ?>> eventSourceType, final Class<? extends AbstractEventSource<?, ?>>... otherEventSourceTypes) {
        this.injector = injector;
        this.eventSourceTypes.add(eventSourceType);
        this.eventSourceTypes.addAll(listOf(otherEventSourceTypes));
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    public EventSourcingResourceFactory(final IDeviceProvider deviceProvider, final IDates dates, final AbstractEventSource<?, ?> eventSource, final AbstractEventSource<?, ?>... otherEventSources) {
        this.injector = null;
        this.eventSources.add(eventSource);
        this.eventSources.addAll(listOf(otherEventSources));
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            if (!SseUtils.isEventSourceRequest(request)) {
                throw new InvalidSseUriException(String.format("URI [%s] is not valid for SSE.", request.getResourceRef().toString()));
            }
            if (!eventSources.isEmpty()) {
                new EventSourcingResource(eventSources, deviceProvider, dates, getContext(), request, response).handle();
            } else {
                new EventSourcingResource(eventSourceTypes.stream().map(eventSourceType -> injector.getInstance(eventSourceType)).collect(toList()), deviceProvider, dates, getContext(), request, response).handle();
            }
        }
    }

}

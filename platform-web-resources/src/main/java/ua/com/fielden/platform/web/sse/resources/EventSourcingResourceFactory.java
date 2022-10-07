package ua.com.fielden.platform.web.sse.resources;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import rx.Observable;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
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

    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final IEventSourceEmitterRegister eseRegister;

    public EventSourcingResourceFactory(final IEventSourceEmitterRegister eseRegister, final IDeviceProvider deviceProvider, final IDates dates) {
        this.eseRegister = eseRegister;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            if (!SseUtils.isEventSourceRequest(request)) {
                throw new InvalidSseUriException(String.format("URI [%s] is not valid for SSE.", request.getResourceRef().toString()));
            }

            new EventSourcingResource(eseRegister, deviceProvider, dates, getContext(), request, response).handle();
        }
    }

}
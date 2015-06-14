package ua.com.fielden.platform.web.sse.resources;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import rx.Observable;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

import com.google.inject.Injector;

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

    public EventSourcingResourceFactory(final Injector injector, final Class<? extends AbstractEventSource<?, ?>> eventSourceType) {
        this.injector = injector;
        this.eventSourceType = eventSourceType;
    }

    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            new EventSourcingResource(injector.getInstance(eventSourceType), getContext(), request, response).handle();
        }
    }

}

package ua.com.fielden.platform.web.sse;

import javax.servlet.http.HttpServletRequest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import rx.Observable;

import com.google.inject.Injector;

/**
 * A factory for a web resource {@link EventSourcingResource} that provides a general purpose implementation for the server-side eventing
 * where events are values of the specified data stream, which is represented by an instance of {@link Observable}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EventSourcingResourceFactory<T, S extends AbstractEventSource<T>> extends Restlet {

    private final Injector injector;
    private final Observable<T> stream;
    private final Class<S> eventSourceType;

    public EventSourcingResourceFactory(final Injector injector, final Observable<T> stream, final Class<S> eventSourceType) {
        this.injector = injector;
        this.stream = stream;
        this.eventSourceType = eventSourceType;
    }

    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            new EventSourcingResource(getContext(), request, response) {

                @Override
                public IEventSource newEventSource(final HttpServletRequest request) {
                    return injector.getInstance(eventSourceType).setStream(stream);
                }

            }.handle();
        }
    }

}

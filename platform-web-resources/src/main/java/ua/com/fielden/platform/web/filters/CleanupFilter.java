package ua.com.fielden.platform.web.filters;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

import com.esotericsoftware.kryo.Kryo;

import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;

/**
 * A HTTP filter that should be used in order to clear various thread-local caches.
 * <p>
 * This filter needs to be wired in the application {@code main} method such as in application {@code Start} classes.
 * <pre>
 * final CleanupFilter cleanupFilter = new CleanupFilter();
 * cleanupFilter.setNext(application);
 * server.setNext(cleanupFilter);
 * </pre>
 *  
 * @author TG Team 
 *
 */
public class CleanupFilter extends Filter {

    @Override
    protected void afterHandle(final Request request, final Response response) {
        Kryo.getContext().reset();
        EntitySerialiser.getContext().reset();

        super.afterHandle(request, response);
    }
}

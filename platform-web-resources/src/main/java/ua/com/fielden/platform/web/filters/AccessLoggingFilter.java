package ua.com.fielden.platform.web.filters;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

/**
 * A HTTP filter that should be used in order to log all authenticated requests.
 * <p>
 * This filter needs to be wired in the application {@code main} method such as in application {@code Start} classes.
 * <pre>
 * final AccessLoggingFilter accessLoggingFilter = new AccessLoggingFilter();
 * accessLoggingFilter.setNext(application);
 * server.setNext(accessLoggingFilter);
 * </pre>
 *  
 * @author TG Team 
 *
 */
public class AccessLoggingFilter extends Filter {

    @Override
    protected void afterHandle(final Request request, final Response response) {
        super.afterHandle(request, response);
        
        final long startedAt = request.getDate().getTime();
        final long finishedAt = System.currentTimeMillis();
        final long totalTimeInMillis = finishedAt - startedAt;
        System.out.printf("Request: %s, %s; client info: %s, %s, %s, %s, %s, %s, %s%n", 
                request.getDate(),
                request.getClientInfo().getAgent(), 
                request.getClientInfo().getUpstreamAddress(),
                request.getClientInfo().isAuthenticated(),
                request.getResourceRef(),
                request.getMethod(),
                request.getClientInfo().getUser(),
                response.getStatus().getCode(),
                totalTimeInMillis);

    }
}

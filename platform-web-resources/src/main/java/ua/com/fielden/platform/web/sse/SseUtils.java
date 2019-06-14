package ua.com.fielden.platform.web.sse;

import org.restlet.Request;

/**
 * A set of utilities for functionality related to Server-Sent Event handling.
 * @author TG Team
 *
 */
public class SseUtils {
    private static final String SSE_URI = "/sse/";

    /**
     * Identifies whether {@code request} is an SSE request by analysing the request URI.
     * @param request
     * @return
     */
    public static boolean isEventSourceRequest(final Request request) {
        return isEventSourceUri(request.getResourceRef().toString());
    }
    
    /**
     * Identifies whether {@code uri} represents an SSE related URI by checking if it follows the established convention of containing {@code /sse/}. 
     * @param uri
     * @return
     */
    public static boolean isEventSourceUri(final String uri) {
        return uri.contains(SSE_URI);
    }

}

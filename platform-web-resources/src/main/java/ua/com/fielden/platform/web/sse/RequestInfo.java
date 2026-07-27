package ua.com.fielden.platform.web.sse;

import jakarta.servlet.http.HttpServletRequest;

import static java.lang.String.format;

/**
 * A structure to represent a HTTP request information for logging and other reporting purposes.
 *
 * @author TG Team
 *
 */
public class RequestInfo {
    public final String resourceRef;
    public final String method;
    public final String address;
    public final String agentName;
    public final String agentVersion;

    public RequestInfo(final HttpServletRequest request) {
        resourceRef = request.getPathInfo().toString(); 
        method = request.getMethod();
        address = request.getRemoteAddr();
        agentName = request.getHeader("user-agent");
        agentVersion = "see agent info";
    }

    @Override
        public String toString() {
            return format("URI %s (method %s) by %s (agent %s, version %s).", resourceRef, method, address, agentName, agentVersion);
        }

}
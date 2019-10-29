package ua.com.fielden.platform.web.application;

import static java.lang.String.format;

import org.restlet.Request;

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
    
    public RequestInfo(final Request request) {
        resourceRef = request.getResourceRef().toString(); 
        method = request.getMethod().getName();
        address = request.getClientInfo().getAddress();
        agentName = request.getClientInfo().getAgentName();
        agentVersion = request.getClientInfo().getAgentVersion(); 
    }

    @Override
        public String toString() {
            return format("URI %s (method %s) by %s (agent %s, version %s).", resourceRef, method, address, agentName, agentVersion);
        }
}

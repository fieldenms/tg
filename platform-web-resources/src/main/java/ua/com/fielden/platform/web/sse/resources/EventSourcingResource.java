package ua.com.fielden.platform.web.sse.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.application.RequestInfo;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResource;
import ua.com.fielden.platform.web.sse.EventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.sse.exceptions.SseException;
import ua.com.fielden.platform.web.utils.ServletUtils;

/**
 * A general purpose web resource for subscribing clients to Server-Side Eventing, which is expressed as constructor argument <code>eventSource</code>.
 *
 * @author TG Team
 */
public class EventSourcingResource extends AbstractWebResource {

    private final Logger logger = Logger.getLogger(this.getClass());
    private final AtomicBoolean shouldKeepGoing = new AtomicBoolean(true);
    private final IUserProvider userProvider;
    private final IEventSourceEmitterRegister eseRegister;

    public EventSourcingResource(
            final IEventSourceEmitterRegister eseRegister,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.userProvider = userProvider;
        this.eseRegister = eseRegister;
    }

    /**
     * Handles client subscription requests.
     *
     * @throws ResourceException
     */
    @Get
    public void subcribeClient() throws ResourceException {
        final String sseIdString = getRequest().getAttributes().get("sseUid").toString();
        final User user = userProvider.getUser();
        // any errors that may occur during subscription or a normal lifecycle after, should result in deregistering and closing of the emitter, created during this request
        try {
            try {
                eseRegister.registerEmitter(user, sseIdString, () -> {
                    try {
                        final HttpServletRequest httpRequest = ServletUtils.getRequest(getRequest());
                        final HttpServletResponse httpResponse = ServletUtils.getResponse(getResponse());
                        makeHandshake(httpResponse);
                        // create an asynchronous context for pushing messages out to the subscribed client
                        final AsyncContext async = httpRequest.startAsync();
                        // Infinite timeout because the continuation is never resumed,
                        // but only completed on close
                        async.setTimeout(0);
                        
                        return new EventSourceEmitter(shouldKeepGoing, async, new RequestInfo(getRequest()));
                    } catch (final IOException ex) {
                         throw new SseException("Could not create a new SSE emitter.", ex);
                    }
                }).ifFailure(Result::throwRuntime);
                
            } catch (final Exception ex) {
                logger.error(ex);
                throw new ResourceException(ex);
            }
            // don't let the restlet end this call, which leads to closing of the current connection
            // TODO this approach is a workaround and should be changed to a better solution as soon as the know-how becomes available
            while (shouldKeepGoing.get()) {
                try {
                    Thread.sleep(2000);
                } catch (final Exception e) {
                    logger.error(e);
                }
            }
        } finally {
            eseRegister.deregisterEmitter(user, sseIdString);
            logger.debug(String.format("SSE subscription for client [%s, %s] completed.", user, sseIdString));
        }

    }

    protected void makeHandshake(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream");
        // By adding this header, and not closing the connection,
        // we disable HTTP chunking, and we can use write()+flush()
        // to send data in the text/event-stream protocol
        response.addHeader("Connection", "close");
        response.flushBuffer();
    }

}

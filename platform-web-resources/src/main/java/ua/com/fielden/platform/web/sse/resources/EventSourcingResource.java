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
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.sse.EventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSource;
import ua.com.fielden.platform.web.utils.ServletUtils;

/**
 * A general purpose web resource for subscribing clients to Server-Side Eventing, which is expressed as constructor argument <code>eventSource</code>.
 *
 * @author TG Team
 */
public class EventSourcingResource extends ServerResource {

    private final Logger logger = Logger.getLogger(this.getClass());
    private final AtomicBoolean shouldKeepGoing = new AtomicBoolean(true);
    private final IEventSource eventSource;

    public EventSourcingResource(
            final IEventSource eventSource,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.eventSource = eventSource;
    }

    /**
     * Handles client subscription requests.
     *
     * @throws ResourceException
     */
    @Get
    public void subcribeClient() throws ResourceException {
        try {
            final HttpServletRequest httpRequest = ServletUtils.getRequest(getRequest());
            final HttpServletResponse httpResponse = ServletUtils.getResponse(getResponse());
            makeHandshake(httpResponse);
            // create an asynchronous context for pushing messages out to the subscribed client
            final AsyncContext async = httpRequest.startAsync();
            // Infinite timeout because the continuation is never resumed,
            // but only completed on close
            async.setTimeout(0);
            final EventSourceEmitter emitter = new EventSourceEmitter(shouldKeepGoing, eventSource, async);
            emitter.scheduleHeartBeat();
            eventSource.onOpen(emitter);
        } catch (final IOException ex) {
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
        logger.debug("Server-Sent Event Restlet completed.");

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

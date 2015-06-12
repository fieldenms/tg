package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


public abstract class EventSourcingResource extends ServerResource {

    private final AtomicBoolean shouldKeepGoing = new AtomicBoolean(true);

    public EventSourcingResource(
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

    }

    public abstract IEventSource newEventSource(HttpServletRequest request);

    @Get
    public void subcribeClient() throws ResourceException {
        final HttpServletRequest  httpRequest = ServletUtils.getRequest(getRequest());
        final HttpServletResponse httpResponse = ServletUtils.getResponse(getResponse());

        try {
            final IEventSource eventSource = newEventSource(httpRequest);
            if (eventSource == null) {
                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
                respond(httpRequest, httpResponse);
                final AsyncContext async = httpRequest.startAsync();//!httpRequest.isAsyncStarted() ? httpRequest.startAsync() : httpRequest.getAsyncContext();
                // Infinite timeout because the continuation is never resumed,
                // but only completed on close
                async.setTimeout(0);
                final EventSourceEmitter emitter = new EventSourceEmitter(shouldKeepGoing, eventSource, async);
                emitter.scheduleHeartBeat();
                eventSource.onOpen(emitter);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
            throw new ResourceException(ex);
        }
        // don't let the restlet end this call, which leads to closing of the current connection
        while (shouldKeepGoing.get()) {
            try {
                Thread.sleep(2000);
            } catch (final Exception e) {

            }
        }
        System.out.println("Server-Sent Event Restlet completed.");

    }

    protected void respond(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/event-stream");

            // By adding this header, and not closing the connection,
            // we disable HTTP chunking, and we can use write()+flush()
            // to send data in the text/event-stream protocol
            response.addHeader("Connection", "close");

            response.flushBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


}

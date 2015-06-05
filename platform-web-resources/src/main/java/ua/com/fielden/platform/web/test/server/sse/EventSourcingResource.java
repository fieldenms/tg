package ua.com.fielden.platform.web.test.server.sse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


public abstract class EventSourcingResource extends ServerResource {

    public EventSourcingResource(
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

    }

    public abstract IEventSource newEventSource(HttpServletRequest request);

    @Get
    @Override
    public Representation get() throws ResourceException {
        final HttpServletRequest  httpRequest = ServletUtils.getRequest(getRequest());
        final HttpServletResponse httpResponse = ServletUtils.getResponse(getResponse());

        try {
            final IEventSource eventSource = newEventSource(httpRequest);
            if (eventSource == null) {
                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
                respond(httpRequest, httpResponse);
                final AsyncContext async = !httpRequest.isAsyncStarted() ? httpRequest.startAsync() : httpRequest.getAsyncContext();
                // Infinite timeout because the continuation is never resumed,
                // but only completed on close
                async.setTimeout(0);
                final EventSourceEmitter emitter = new EventSourceEmitter(eventSource, async);
                emitter.scheduleHeartBeat();
                open(eventSource, emitter);
            }
        } catch (final IOException ex) {
            throw new ResourceException(ex);
        }
        return null;
    }

    protected void respond(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/event-stream");
            response.addHeader("cache-control", "no-cache");
            response.addHeader("connection", "keep-alive");
            response.flushBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected void open(final IEventSource eventSource, final IEmitter emitter) throws IOException {
        eventSource.onOpen(emitter);
    }

}

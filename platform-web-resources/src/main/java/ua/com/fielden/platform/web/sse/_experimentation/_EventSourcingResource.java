package ua.com.fielden.platform.web.sse._experimentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.utils.ServletUtils;

public abstract class _EventSourcingResource extends ServerResource implements Runnable {

    private ScheduledFuture<?> heartBeat;
    private ServletOutputStream output;
    private HttpServletResponse httpResponse;
    private int count;
    private boolean shouldKeepGoing;


    public _EventSourcingResource(
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

    }

    public abstract _EventSource newEventSource(HttpServletRequest request);

    @Get
    public void get_method() throws Exception {
        final HttpServletRequest httpRequest = ServletUtils.getRequest(getRequest());
        httpResponse = ServletUtils.getResponse(getResponse());

        try {
            final _EventSource eventSource = newEventSource(httpRequest);
            if (eventSource == null) {
                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
                respond(httpRequest, httpResponse);
                //final AsyncContext async = httpRequest.startAsync();
                //async.setTimeout(0);

                //final Continuation continuation = ContinuationSupport.getContinuation(httpRequest);
                // Infinite timeout because the continuation is never resumed,
                // but only completed on close
                //continuation.setTimeout(0L);
                //continuation.suspend(httpResponse);

                //final EventSourceEmitterOld emitter = new EventSourceEmitterOld(eventSource, continuation);
                //emitter.scheduleHeartBeat();
                //open(eventSource, emitter);
            }
        } catch (final IOException ex) {
            throw new ResourceException(ex);
        }


        System.out.println("Connection established");
        shouldKeepGoing = true;
        count = 0;
        output = httpResponse.getOutputStream();
        heartBeat = scheduler.schedule(this, 3, TimeUnit.SECONDS);
        while (shouldKeepGoing) {
            try {
                Thread.sleep(2000);
            } catch (final Exception e) {

            }
        }
        System.out.println("Restlet work completed, " + count + " events pushed.");

    }

    @Override
    public void run() {
        try {
            data(output, new Date().toString());
            httpResponse.flushBuffer();
            count++;
            heartBeat = scheduler.schedule(this, 3, TimeUnit.SECONDS);
        } catch (final IOException e) {
            System.out.println("Connection interrupted. Sent " + count + " events.");
            shouldKeepGoing = false;
        }


    }

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] EVENT_FIELD = "event: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA_FIELD = "data: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COMMENT_FIELD = ": ".getBytes(StandardCharsets.UTF_8);


    public void data(final ServletOutputStream output, final String data) throws IOException {
        synchronized (this) {
            final BufferedReader reader = new BufferedReader(new StringReader(data));
            String line;
            while ((line = reader.readLine()) != null) {
                output.write(DATA_FIELD);
                output.write(line.getBytes(StandardCharsets.UTF_8));
                output.write(CRLF);
            }
            output.write(CRLF);
        }
    }


    private static final Charset UTF_8 = Charset.forName("UTF-8");

    protected void respond(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(UTF_8.name());
            response.setContentType("text/event-stream");

            // By adding this header, and not closing the connection,
            // we disable HTTP chunking, and we can use write()+flush()
            // to send data in the text/event-stream protocol
            response.addHeader("Connection", "close");

            //response.addHeader("cache-control", "no-cache");
            //response.addHeader("connection", "keep-alive");

            response.flushBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected void open(final _EventSource eventSource, final _EventSource.Emitter emitter) throws IOException {
        eventSource.onOpen(emitter);
    }

}

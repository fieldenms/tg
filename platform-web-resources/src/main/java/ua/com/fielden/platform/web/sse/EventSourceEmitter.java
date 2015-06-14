package ua.com.fielden.platform.web.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;

/**
 * This EventSource emitter is taken from Jetty source in the attempt to make it working with Restlet.
 * <p>
 * TODO: Need to support message id to be able to send the client all the missed messages, and not just to restart sending messages from whatever happens to be the current. TODO:
 * Most likely it should be open sourced. The same goes about a corresponding Restlet web resource.
 *
 * @author TG Team
 *
 */
public final class EventSourceEmitter implements IEmitter, Runnable {

    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] EVENT_FIELD = "event: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA_FIELD = "data: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COMMENT_FIELD = ": ".getBytes(StandardCharsets.UTF_8);

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int heartBeatPeriod = 5;

    private final IEventSource eventSource;
    private final AsyncContext async;
    private final ServletOutputStream output;
    private Future<?> heartBeat;
    private boolean closed;
    private final AtomicBoolean shouldResourceThreadBeBlocked;

    /**
     * The use of <code>stopResourceThread</code> is a workaround at this stage to prevent restlet from closing the connection with the client by means of blocking its thread by
     * putting it to sleep.
     *
     * @param shouldResourceThreadBeBlocked
     * @param eventSource
     * @param async
     * @throws IOException
     */
    public EventSourceEmitter(final AtomicBoolean shouldResourceThreadBeBlocked, final IEventSource eventSource, final AsyncContext async) throws IOException {
        this.shouldResourceThreadBeBlocked = shouldResourceThreadBeBlocked;
        this.eventSource = eventSource;
        this.async = async;
        this.output = async.getResponse().getOutputStream();
    }

    @Override
    public void event(final String name, final String data) {
        synchronized (this) {
            try {
                output.write(EVENT_FIELD);
                output.write(name.getBytes(StandardCharsets.UTF_8));
                output.write(CRLF);
                data(data);
            } catch (final IOException e) {
                close();
            }

        }
    }

    @Override
    public void data(final String data) throws IOException {
        synchronized (this) {
            try {
                final BufferedReader reader = new BufferedReader(new StringReader(data));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.write(DATA_FIELD);
                    output.write(line.getBytes(StandardCharsets.UTF_8));
                    output.write(CRLF);
                }
                output.write(CRLF);
                flush();
            } catch (final IOException e) {
                close();
            }
        }
    }

    @Override
    public void comment(final String comment) throws IOException {
        synchronized (this) {
            try {
                output.write(COMMENT_FIELD);
                output.write(comment.getBytes(StandardCharsets.UTF_8));
                output.write(CRLF);
                output.write(CRLF);
                flush();
            } catch (final IOException e) {
                close();
            }
        }
    }

    @Override
    public void run() {
        // If the other peer closes the connection, the first
        // flush() should generate a TCP reset that is detected
        // on the second flush()
        try {
            synchronized (this) {
                output.write('\r');
                flush();
                output.write('\n');
                flush();
            }
            // reschedule heartbeat
            scheduleHeartBeat();
        } catch (final IOException ex) {
            close();
        }
    }

    protected void flush() throws IOException {
        async.getResponse().flushBuffer();
    }

    @Override
    public void close() {
        synchronized (this) {
            closed = true;
            heartBeat.cancel(false);
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }
        async.complete();
        shouldResourceThreadBeBlocked.set(false);
        eventSource.onClose();
    }

    public void scheduleHeartBeat() {
        synchronized (this) {
            if (!closed) {
                heartBeat = scheduler.schedule(this, heartBeatPeriod, TimeUnit.SECONDS);
            }
        }
    }
}
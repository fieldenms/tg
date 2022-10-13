package ua.com.fielden.platform.web.sse;

import static java.lang.String.format;

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

import org.apache.log4j.Logger;

import ua.com.fielden.platform.web.application.RequestInfo;

/**
 * Event source emitter represents a connection to a web client for pushing SSE events to that client.
 * It other words, this is just a pipe through which all SSE messages get sent to a web client, and which has no knowledge as to what kind of messages are sent or what event sources have produced them.
 * <p>
 * In the current SSE architecture, {@link EventSourceDispatchingEmitter} is responsible for dispatching SSE events to event source emitters that are registered with it. All emitters should be registered with a dispatcher instance.
 * This was not always the case – before that, each emitter was in one-2-one correspondence with an event source, and the lifecycle of every such event source was tight to the lifecycle of an emitter.
 * In the current SSE architecture, only closing of {@link EventSourceDispatchingEmitter}, which signifies the end of its life, leads to disconnecting of all the event sources.
 * And there can be more than 1 event source, which could push events to the same emitter.
 * <p>
 * The original implementation for Event Source emitter was taken from the Jetty source in the attempt to make it working with Restlet.
 * <p>
 * TODO: Need to support message id to be able to send the client all the missed messages, and not just to restart sending messages from whatever happens to be the current.
 *
 * @author TG Team
 *
 */
public final class EventSourceEmitter implements IEventSourceEmitter, Runnable {

    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] EVENT_FIELD = "event: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA_FIELD = "data: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COMMENT_FIELD = "comment: ".getBytes(StandardCharsets.UTF_8);

    private final Logger logger = Logger.getLogger(getClass());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int heartBeatPeriod = 5;

    private final AsyncContext async;
    private final ServletOutputStream output;
    private Future<?> heartBeat;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean shouldResourceThreadBeBlocked;
    private final RequestInfo info;

    /**
     * The use of {@code stopResourceThread} is a workaround at this stage to prevent Restlet from closing the connection with the client by means of blocking its thread by putting it to sleep.
     *
     * @param shouldResourceThreadBeBlocked
     * @param async
     * @param info
     * @throws IOException
     */
    public EventSourceEmitter(final AtomicBoolean shouldResourceThreadBeBlocked, final AsyncContext async, final RequestInfo info) throws IOException {
        this.shouldResourceThreadBeBlocked = shouldResourceThreadBeBlocked;
        this.async = async;
        this.output = async.getResponse().getOutputStream();
        this.info = info;
        logger.info(format("Started event source emitter: %s", info.toString()));
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
            try (final BufferedReader reader = new BufferedReader(new StringReader(data))) {
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
        try {
            if (!closed.getAndSet(true)) {
                synchronized (this) {
                    heartBeat.cancel(false);
                    if (scheduler != null) {
                        scheduler.shutdown();
                    }
                }
                async.complete();
            }
        } finally {
            logger.info(format("Closed event source emitter: %s", info.toString()));
            shouldResourceThreadBeBlocked.set(false);
        }
    }

    public void scheduleHeartBeat() {
        synchronized (this) {
            if (!closed.get()) {
                heartBeat = scheduler.schedule(this, heartBeatPeriod, TimeUnit.SECONDS);
            }
        }
    }

}
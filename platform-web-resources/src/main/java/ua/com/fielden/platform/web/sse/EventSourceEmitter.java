package ua.com.fielden.platform.web.sse;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Event source emitter represents a connection to a web client for pushing SSE events to that client. It other words, this is just a pipe through which all SSE messages get sent
 * to a web client, and which has no knowledge as to what kind of messages are sent or what event sources have produced them.
 * <p>
 * In the current SSE architecture, {@link EventSourceDispatchingEmitter} is responsible for dispatching SSE events to event source emitters that are registered with it. All
 * emitters should be registered with a dispatcher instance. This was not always the case – before that, each emitter was in one-2-one correspondence with an event source, and the
 * lifecycle of every such event source was tight to the lifecycle of an emitter. In the current SSE architecture, only closing of {@link EventSourceDispatchingEmitter}, which
 * signifies the end of its life, leads to disconnecting of all the event sources. And there can be more than 1 event source, which could push events to the same emitter.
 * <p>
 * This implementation was inspired by <a href='https://github.com/eclipse/jetty.project/blob/jetty-9.4.x/jetty-servlets/src/main/java/org/eclipse/jetty/servlets/EventSourceServlet.java'>EventSourceServlet</a>.
 * <p>
 * TODO: Need to support message id to be able to send the client all the missed messages, and not just to restart sending messages from whatever happens to be the current.
 *
 * @author TG Team
 *
 */
public final class EventSourceEmitter implements IEventSourceEmitter {

    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] EVENT_FIELD = "event: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA_FIELD = "data: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COMMENT_FIELD = "comment: ".getBytes(StandardCharsets.UTF_8);

    private final Logger logger = getLogger(getClass());

    private final AsyncContext async;
    private final ServletOutputStream output;

    private final int heartbeatFrequencyInSeconds;
    private final ScheduledExecutorService heartbeatScheduler;
    private Future<?> heartbeatTask; // mutable as it gets reassigned upon hear beat rescheduling
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final RequestInfo info;

    private final Optional<Runnable> maybeCloseCallback;

    /**
     * The use of {@code stopResourceThread} is a workaround at this stage to prevent Restlet from closing the connection with the client by means of blocking its thread by putting it to sleep.
     *
     * @param async
     * @param info
     * @param heartbeatScheduler
     * @param heartbeatFrequencyInSeconds
     * @param closeCallback
     * @throws IOException
     */
    public EventSourceEmitter(final AsyncContext async, final RequestInfo info, final ScheduledExecutorService heartbeatScheduler, final int heartbeatFrequencyInSeconds, final Runnable closeCallback) throws IOException {
        this.async = async;
        this.output = async.getResponse().getOutputStream();
        this.info = info;
        this.heartbeatFrequencyInSeconds = heartbeatFrequencyInSeconds;
        this.heartbeatScheduler = heartbeatScheduler;
        this.heartbeatTask = scheduleHeartbeat(heartbeatScheduler, heartbeatFrequencyInSeconds);
        this.maybeCloseCallback = Optional.ofNullable(closeCallback);
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

    private void flush() throws IOException {
        async.getResponse().flushBuffer();
    }

    @Override
    public void close() {
        if (!closed.getAndSet(true)) {
            try {
                synchronized (this) {
                    heartbeatTask.cancel(false);
                }
                async.complete();
                maybeCloseCallback.ifPresent(cc -> cc.run());
            } finally {
                logger.info(format("Closed event source emitter: %s", info.toString()));
            }
        }
    }

    /**
     * Performs a heart beat to let the client side know what the connection is still active.
     */
    private void doHeartBeat() {
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
            // reschedule heartbeat, but only if this emitter was not closed yet
            if (!closed.get()) {
                heartbeatTask = scheduleHeartbeat(heartbeatScheduler, heartbeatFrequencyInSeconds);
            }
        } catch (final IOException ex) {
            close();
        }
    }

    /**
     * A helper factory method to schedule execution of {@link #doHeartBeat()}.
     *
     * @return scheduled task
     */
    private ScheduledFuture<?> scheduleHeartbeat(final ScheduledExecutorService scheduler, final int heartBeatPeriod) {
        return scheduler.schedule(this::doHeartBeat, heartBeatPeriod, TimeUnit.SECONDS);
    }

}
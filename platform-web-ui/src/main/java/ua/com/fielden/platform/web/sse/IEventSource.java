package ua.com.fielden.platform.web.sse;

import java.io.IOException;

/**
 * {@link IEventSource} is the passive half of an event source connection, as defined by the <a href="http://www.w3.org/TR/eventsource/">EventSource Specification</a>.
 * <p>
 * {@link IEventSourceEmitter} is the active half of the connection and allows to operate on the connection.
 * <p>
 * {@link IEventSource} allows applications to be notified of events happening on the connection.
 */
public interface IEventSource {

    /**
     * A method that is invoked when this event source needs to connect to {@code emitter}, through which events should be sent.
     * <p>
     *
     * @param emitter
     *            a {@link IEventSourceEmitter} instance, through which events are sent.
     * @throws IOException
     *             if the implementation of the method throws such exception
     */
    void connect(final IEventSourceEmitter emitter);

    /**
     * A method that is invoked when this event source should be disconnected from the {@code emitter}, passed into {@link #connect(IEventSourceEmitter)}.
     * This usually needs to be done when this even source has completed its work (i.e., no new events need to be observed and sent to a web client).
     */
    void disconnect();

}
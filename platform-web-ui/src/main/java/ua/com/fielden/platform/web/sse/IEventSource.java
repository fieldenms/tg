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
     * A method that is invoked when an event source connection is opened, which subscribes to the evens from the {@code emitter}.
     * <p>
     *
     * @param emitter
     *            the {@link IEventSourceEmitter} instance that allows to operate on the connection
     * @throws IOException
     *             if the implementation of the method throws such exception
     */
    void subscribe(final IEventSourceEmitter emitter) throws IOException;

    /**
     * A method that is invoked when an event source connection is closed, and thus this even source needs to unsubscribe from the {@code emitter}, passed into {@link #subscribe(IEventSourceEmitter)}.
     */
    void unsubscribe();

}
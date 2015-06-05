package ua.com.fielden.platform.web.test.server.sse;

import java.io.IOException;

/**
 * {@link IEventSource} is the passive half of an event source connection, as defined by the <a href="http://www.w3.org/TR/eventsource/">EventSource Specification</a>.
 * <p>
 * {@link IEmitter} is the active half of the connection and allows to operate on the connection.
 * <p>
 * {@link IEventSource} allows applications to be notified of events happening on the connection; two events are being notified: the opening of the event source connection, where
 * method {@link IEventSource#onOpen(IEmitter)} is invoked, and the closing of the event source connection, where method {@link IEventSource#onClose()} is invoked.
 */
public interface IEventSource
{
    /**
     * Callback method invoked when an event source connection is opened.
     * <p>
     *
     * @param emitter
     *            the {@link IEmitter} instance that allows to operate on the connection
     * @throws IOException
     *             if the implementation of the method throws such exception
     */
    public void onOpen(final IEmitter emitter) throws IOException;

    /**
     * Callback method invoked when an event source connection is closed.
     */
    public void onClose();

}

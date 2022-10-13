package ua.com.fielden.platform.web.sse;

import java.io.IOException;

import ua.com.fielden.platform.rx.AbstractSubjectKind;

/**
 * {@link IEventSource} is the passive half of an event source connection, as defined by the <a href="http://www.w3.org/TR/eventsource/">EventSource Specification</a>, which can only push notifications to a single emitter.
 * An emitter of type {@link IEventSourceEmitter} is the active half of a connection, and is responsible for sending events of the wire.
 * <p>
 * An instance of {@link IEventSource} acts as a means to push notifications, published to concrete implementations of {@link AbstractSubjectKind} and subscribed to by this event source, to an emitter {@link IEventSourceEmitter} that was connected to this event source instance.
 * A single event source can push notification to a single emitter.
 * However, it is possible for several different event sources, potentially subscribed to different subject kinds, to be connected to the same emitter and thus push notifications to the same web client through the same connection.
 * <p>
 * It should also be noted that if an emitter can dispatch events to other emitters, then by connecting to such a "broadcasting" emitter, an event source can push notifications to multiple emitters, potentially associated with different web clients.
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
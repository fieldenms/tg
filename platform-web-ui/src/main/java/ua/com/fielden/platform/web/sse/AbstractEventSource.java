package ua.com.fielden.platform.web.sse;

import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import ua.com.fielden.platform.rx.IObservableKind;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * A base class for custom event sources that are subscribed to the specified data streams (aka observable) and emit respective values to the client.
 * Instances of derived classes should not have a singleton scope (!) as they should be instantiated on per subscription request basis.
 * <p>
 * Values that are received from the data stream are of type <code>T</code>.
 * Each such value should be converted to a string message in accordance with EventSourcing <a href="https://developer.mozilla.org/en-US/docs/Server-sent_events/Using_server-sent_events#Event_stream_format">spec</a> in order to be sent to the client.
 * For this, abstract method {@link #eventToData(Object)} needs to be implemented by descendants.
 * <p>
 * Also, method {@link #getStream()} can be overridden if required in order to apply any necessary transformations to the stream before subscribing to it.
 *
 * @author TG Team
 *
 * @param <T> -- event type.
 * @param <OK> -- observable kind type that produces an observable to be subscribed to for receiving events from.
 */
public abstract class AbstractEventSource<T, OK extends IObservableKind<T>> implements IEventSource {

    /**
     * The emitter that is used for sending messages back to the client.
     * It is provided by the web resource in response to a subscription request from the client.
     */
    private IEventSourceEmitter emitter;
    /**
     * A subscription to the sever-side data stream, which is mainly used for unsubscribing.
     */
    private Subscription subscription;
    /**
     * The original stream that is provided during instantiation to be subscribed to.
     * However, the actual subscription might be to a stream resulting from transformations of this one.
     */
    private Observable<T> stream;

    /**
     * A JSON serialiser needed to convert a map of data into a String.
     */
    private final ISerialiser serialiser;

    private final Logger logger = getLogger(this.getClass());


    protected AbstractEventSource(final OK observableKind, final ISerialiser serialiser) {
        this.stream = observableKind.asObservable();
        if (stream == null) {
            throw new IllegalArgumentException("Event stream is required.");
        }
        this.serialiser = serialiser;
    }

    @Override
    public final void connect(final IEventSourceEmitter emitter) {
        logger.debug("client subscription in progress...");
        this.emitter = emitter;
        this.subscription = getStream().subscribe(new EventObserver());

        try {
            this.emitter.event("connection", "established");
            logger.debug("client subscribed successfully");
        } catch (final IOException ex) {
            throw new SseException("Could not send connection event.", ex);
        }
    }

    /**
     * By default, returns the original stream as assigned externally.
     * This method could be overridden to derive a new stream of the same event type for this event source to be subscribed to.
     * Filtering would be a good example.
     *
     * @return
     */
    protected Observable<T> getStream() {
        if (stream == null) {
            throw new IllegalStateException("Event stream has not been specified.");
        }
        return stream;
    }

    /// A method for converting values of type `T` that are received from the data stream to a string representation suitable for
    /// the `data` field of server-side eventing.
    ///
    /// Short values can be represented as a single string without new lines.
    /// Long values should be represented as a single string with new lines embedded in the text to indicate split points for composing data entries of the message to be pushed out.
    ///
    /// Refer to the [event stream format](https://developer.mozilla.org/en-US/docs/Server-sent_events/Using_server-sent_events#Event_stream_format) for more details.
    ///
    protected abstract Map<String, Object> eventToData(final T event);

    /// Unsubscribes from a stream of events this event source was observing.
    ///
    @Override
    public void disconnect() {
        logger.debug("Client subscription is being disconnected.");
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * A convenience implementation for an observer to receive events from the subscribed to data stream with immediate dispatch of received events to the client.
     */
    private final class EventObserver implements Observer<T> {

        @Override
        public void onCompleted() {
            logger.debug("Event source [%s] completed.".formatted(AbstractEventSource.this.getClass().getName()));
        }

        @Override
        public void onError(final Throwable error) {
            try {
                emitter.event("exception", error.getMessage());
            } catch (final Exception ex) {
                logger.error(ex);
            }
        }

        @Override
        public void onNext(final T value) {
            try {
                final Map<String, Object> data = eventToData(value);
                data.put("eventSourceClass", AbstractEventSource.this.getClass().getName());
                final byte [] serialisedData = serialiser.serialise(data);
                emitter.data(new String(serialisedData, StandardCharsets.UTF_8));
            } catch (final Exception ex) {
                logger.error(ex);
            }
        }
    }

}
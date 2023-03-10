package ua.com.fielden.platform.web.sse;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

/**
 * {@link IEventSourceEmitter} implementation that acts as a dispatching emitter, which dispatches events to registered emitters.
 * Every emitter is added on a request from a web client (every client makes such request), and is associated with a specific user and a unique identifier.
 * There can potentially be multiple emitters for the same user. For example, a user who loads an application in 2 browser tabs would have 2 separate emitters associated with that user.
 * <p>
 * At this stage dispatching happens by means of broadcasting every event to all emitters.
 * However, in the future, it is planned to support sending events to emitters, associated with specific users.
 * <p>
 * Another important role for this class, is to instantiate and register event sources that are specified at the level of Entity Centre configurations.
 * All such event sources get connected to an instance of this class, which ensures that any emitter registered with this class will have events from all the event sources dispatched to them.
 * <p>
 * By design, there should be only a single instance of this class per application – one dispatching emitter per application.
 *
 * @author TG Team
 *
 */
public class EventSourceDispatchingEmitter implements IEventSourceEmitter, IEventSourceEmitterRegister {

    private static final Logger LOGGER = Logger.getLogger(EventSourceDispatchingEmitter.class);

    /**
     * A register of emitters. The key is a pair of user id and a client SSE id.
     * {@link ConcurrentHashMap} is used as the register to support the concurrent nature of such register.
     * It makes it thread-safe to register new emitters, close emitters and dispatch events to emitters concurrently.
     */
    private final ConcurrentHashMap<T2<Long, String>, IEventSourceEmitter> register = new ConcurrentHashMap<>(100);

    /**
     * Controls the state of this dispatching emitter of whether it is open for registration of new emitters and can dispatch events.
     * This is required to ensure that no new emitters get registered and no new events are dispatched if the dispatcher was already closed or is being closed.
     */
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    /**
     * A helper function that creates a register key from {@code user} and {@code sseUid}.
     */
    private static T2<Long, String> key(final User user, final String sseUid) {
        if (user == null) {
            throw new SseException("A user is required to register an SSE emitter.");
        }
        return t2(user.getId(), sseUid);
    }

    /**
     * A collection of event sources, specified for various Entity Centres.
     * The only reason for this collection is to prevent GC from collecting instantiated event sources, which are required for SSE eventing.
     */
    private final Map<Class<? extends IEventSource>, IEventSource> eventSources = new HashMap<>();

    /**
     * Creates and registers an instance of {@code eventSourceClass}, but only if such SSE class was not instantiated before.
     * SSE classes may get specified as part of Entity Centre configurations.
     *
     * @param eventSourceClass
     * @param eventSourceSupplier
     * @return
     * @throws IOException
     */
    public EventSourceDispatchingEmitter createAndRegisterEventSource(final Class<? extends IEventSource> eventSourceClass, final Supplier<IEventSource> eventSourceSupplier) throws IOException {
        if (isActive.get()) {
            eventSources.computeIfAbsent(eventSourceClass, argNotUsed -> {
                LOGGER.info(format("Registering event source [%s].", eventSourceClass.getName()));
                final IEventSource eventSource = eventSourceSupplier.get();
                eventSource.connect(this);
                return eventSource;});
        } else {
            LOGGER.info("The dispatcher is inactive and no new event sources can be registered.");
        }

        return this;
    }

    @Override
    public Result registerEmitter(final User user, final String sseUid, final Supplier<IEventSourceEmitter> emitterFactory) {
        if (isActive.get()) {
            final IEventSourceEmitter emitter = register.computeIfAbsent(key(user, sseUid), argNotUsed -> emitterFactory.get());
            logRegisterSize();
            return successful(emitter);
        }
        return failure("The dispatcher is inactive and no new emitters can be registered.");
    }

    @Override
    public void deregisterEmitter(final User user, final String sseUid) {
        LOGGER.info(format("Deregistering event emitter for web client [%s, %s].", user, sseUid));
        // no exceptions are expected during the emitter removal and closing, but let's be defensive
        // and because we cannot do much in such a case, we simply log the error for further analysis
        try {
            final IEventSourceEmitter emitter = register.remove(key(user, sseUid));
            if (emitter != null) {
                emitter.close();
            }
        } catch (final Throwable ex) {
            LOGGER.error(format("Deregistering event emitter for web client [%s, %s] resulted in error.", user, sseUid), ex);
        } finally {
            logRegisterSize();
        }
    }

    /**
     * A helper method to report the number of SSE connections – a distinct by user and a total number.
     */
    private void logRegisterSize() {
        final KeySetView<T2<Long, String>, IEventSourceEmitter> keySet = register.keySet();
        final long distinctUserConnections = keySet.stream().map(t2 -> t2._1).distinct().count();
        final long totalConnections =  keySet.size();
        LOGGER.info(format("SSE connections: [%s] distinct, [%s] total.", distinctUserConnections, totalConnections));
    }

    @Override
    public IEventSourceEmitter getEmitter(final User user, final String sseUid) {
        return register.get(key(user, sseUid));
    }

    /**
     * Broadcasts an event to all registered emitters (i.e., clients).
     * This method is thread-safe and could in practice get invoked by multiple threads.
     * <p>
     * Iterating over emitters, which are stored in a concurrent map, is thread-safe with "weak consistency".
     * This means that iterators obtained for {@link ConcurrentHashMap} can tolerate concurrent modification, traverses elements as they existed when an iterator was constructed and may (but not guaranteed to) reflect modifications to the collection after the construction of an iterator.
     */
    @Override
    public void event(final String eventName, final String data) throws IOException {
        if (isActive.get()) {
            for(final IEventSourceEmitter emitter: register.values()) {
                emitter.event(eventName, data);
            }
        } else {
            LOGGER.info("The dispatcher is inactive and no new events can be dispatched.");
        }
    }

    /**
     * Broadcasts {@code data} to all registered emitters (i.e., clients).
     * This method is thread-safe and could in practice get invoked by multiple threads, as per explanation in {@link #event(String, String)}.
     */
    @Override
    public void data(final String data) throws IOException {
        if (isActive.get()) {
            for (final IEventSourceEmitter emitter : register.values()) {
                emitter.data(data);
            }
        } else {
            LOGGER.info("The dispatcher is inactive and no new data can be dispatched.");
        }
    }

    /**
     * Broadcasts {@code comment} to all registered emitters (i.e., clients).
     * This method is thread-safe and could in practice get invoked by multiple threads, as per explanation in {@link #event(String, String)}.
     */
    @Override
    public void comment(final String comment) throws IOException {
        if (isActive.get()) {
            for (final IEventSourceEmitter emitter : register.values()) {
                emitter.comment(comment);
            }
        } else {
            LOGGER.info("The dispatcher is inactive and no new comments can be dispatched.");
        }
    }

    /**
     * Removes and closes all emitters, registered previously.
     */
    @Override
    public void close() {
        if (isActive.getAndSet(false)) {
            LOGGER.info("Disconnecting all event sources...");
            for (final Iterator<IEventSource> iter = eventSources.values().iterator(); iter.hasNext();) {
                final IEventSource eventSource = iter.next();
                try {
                    eventSource.disconnect();
                    iter.remove();
                } catch (final Throwable ex) {
                    LOGGER.warn(format("Non critical error during closing of emitters."), ex);
                }
            }

            LOGGER.info("Closing all emitters...");
            for (final Iterator<IEventSourceEmitter> iter = register.values().iterator(); iter.hasNext();) {
                final IEventSourceEmitter emitter = iter.next();
                iter.remove();
                try {
                    emitter.close();
                } catch (final Throwable ex) {
                    LOGGER.warn(format("Non critical error during closing of emitters."), ex);
                }
            }

        }
    }

}
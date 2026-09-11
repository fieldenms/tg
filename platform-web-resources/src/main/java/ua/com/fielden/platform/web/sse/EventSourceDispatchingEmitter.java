package ua.com.fielden.platform.web.sse;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/// [IEventSourceEmitter] implementation that acts as a dispatching emitter, which dispatches events to registered emitters.
/// Every emitter is added on a request from a web client (every client makes such request), and is associated with a specific user and a unique identifier.
/// There can potentially be multiple emitters for the same user.
/// For example, a user who loads an application in 2 browser tabs would have 2 separate emitters associated with that user.
///
/// At this stage dispatching happens by means of broadcasting every event to all emitters.
/// However, in the future, it is planned to support sending events to emitters, associated with specific users.
///
/// Another important role for this class, is to instantiate and register event sources that are specified at the level of Entity Centre configurations.
/// All such event sources get connected to an instance of this class.
/// This ensures that any emitter registered with this class will have events from all the event sources dispatched to them.
///
/// By design, there should be only a single instance of this class per application – one dispatching emitter per application.
///
public class EventSourceDispatchingEmitter implements IEventSourceEmitter, IEventSourceEmitterRegister {

    private static final Logger LOGGER = getLogger(EventSourceDispatchingEmitter.class);

    /// The name of the SSE event used to announce the current application version to a client upon establishing a connection.
    /// The client (see `tg-event-source.js`) listens for an event with this exact name.
    ///
    public static final String APP_VERSION_EVENT_NAME = "application-version";

    private static final int APP_VERSION_MAX_ATTEMPTS = 5;

    private static final long APP_VERSION_RETRY_DELAY_MILLIS = 60_000L;

    /// A register of emitters. The key is a pair of user id and a client SSE id.
    /// [ConcurrentHashMap] is used as the register to support the concurrent nature of such register.
    /// It makes it thread-safe to register new emitters, close emitters and dispatch events to emitters concurrently.
    ///
    private final ConcurrentHashMap<T2<Long, String>, IEventSourceEmitter> register = new ConcurrentHashMap<>(100);

    /// Controls the state of this dispatching emitter of whether it is open for registration of new emitters and can dispatch events.
    /// This is required to ensure that no new emitters get registered and no new events are dispatched if the dispatcher was already closed or is being closed.
    ///
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    /// Supplies the current application version, announced to each client upon establishing an SSE connection.
    /// A client uses this to detect that a newer application version has been deployed since it was loaded.
    ///
    private final Supplier<String> appVersionSupplier;

    /// The application version, resolved at most once, or `null` while unresolved.
    /// It identifies the deployed build, and so is constant for the lifetime of this process.
    ///
    private volatile String appVersion;

    /// Ensures that resolution of the application version is started only once.
    ///
    private final AtomicBoolean isAppVersionResolutionStarted = new AtomicBoolean(false);

    /// Creates a dispatching emitter.
    ///
    /// @param appVersionSupplier supplier of String-based version to be announced to each client upon establishing an SSE connection
    ///
    public EventSourceDispatchingEmitter(final Supplier<String> appVersionSupplier) {
        this.appVersionSupplier = requireNonNull(appVersionSupplier);
    }

    /// A helper function that creates a register key from `user` and `sseUid`.
    ///
    private static T2<Long, String> key(final User user, final String sseUid) {
        if (user == null) {
            throw new SseException("A user is required to register an SSE emitter.");
        }
        return t2(user.getId(), sseUid);
    }

    /// A collection of event sources, specified for various Entity Centres.
    /// The only reason for this collection is to prevent GC from collecting instantiated event sources, which are required for SSE eventing.
    ///
    private final Map<Class<? extends IEventSource>, IEventSource> eventSources = new HashMap<>();

    /// Creates and registers an instance of `eventSourceClass`, but only if such SSE class was not instantiated before.
    /// SSE classes may get specified as part of Entity Centre configurations.
    ///
    public EventSourceDispatchingEmitter createAndRegisterEventSource(final Class<? extends IEventSource> eventSourceClass, final Supplier<IEventSource> eventSourceSupplier) throws IOException {
        if (isActive.get()) {
            eventSources.computeIfAbsent(eventSourceClass, argNotUsed -> {
                LOGGER.info(() -> "Registering event source [%s].".formatted(eventSourceClass.getName()));
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
        LOGGER.info(() -> "Registering event emitter for web client [%s, %s].".formatted(user, sseUid));
        if (isActive.get()) {
            // `computeIfAbsent` runs its mapping function only for a previously unseen client, i.e., a new or re-established connection.
            // The application version is announced only for such new emitters.
            final var isNewEmitter = new MutableBoolean(false);
            final var emitter = register.computeIfAbsent(key(user, sseUid), argNotUsed -> {
                isNewEmitter.setTrue();
                return emitterFactory.get();
            });
            if (isNewEmitter.isTrue()) {
                announceAppVersion(emitter);
            }
            logRegisterSize();
            return successful(emitter);
        }
        return failure("The dispatcher is inactive and no new emitters can be registered.");
    }

    /// Announces the current application version, if any, to `emitter`.
    /// This lets a client detect that a newer application version has been deployed since it was loaded.
    /// Announcing off the request thread keeps a failed write from closing the connection during its own registration.
    ///
    private void announceAppVersion(final IEventSourceEmitter emitter) {
        final var resolvedAppVersion = appVersion;
        if (resolvedAppVersion != null) {
            runAsync(() -> emitAppVersion(emitter, resolvedAppVersion));
        } else if (isAppVersionResolutionStarted.compareAndSet(false, true)) {
            runAsync(() -> resolveAppVersion(APP_VERSION_MAX_ATTEMPTS));
        }
    }

    /// Emits `version` to `emitter`, unless `version` is empty.
    ///
    private void emitAppVersion(final IEventSourceEmitter emitter, final String version) {
        if (!isBlank(version)) {
            try {
                emitter.event(APP_VERSION_EVENT_NAME, version);
            } catch (final Throwable ex) {
                // A production emitter closes itself upon a write failure, so the client reconnects and is announced to again.
                LOGGER.warn(() -> "Could not announce application version [%s] to an SSE client.".formatted(version), ex);
            }
        }
    }

    /// Resolves the application version and announces it to all registered emitters, retrying upon failure.
    ///
    /// Resolution runs off the request thread, so that a throwing [#appVersionSupplier] cannot affect SSE.
    /// Retries are scheduled, for otherwise the remaining attempts would never happen while all clients stay connected.
    /// The guard is as wide as [Throwable], because a misconfigured deployment fails with an [Error], not an exception.
    ///
    private void resolveAppVersion(final int attemptsLeft) {
        try {
            final var suppliedAppVersion = appVersionSupplier.get();
            final var resolvedAppVersion = suppliedAppVersion == null ? "" : suppliedAppVersion;
            appVersion = resolvedAppVersion;
            for (final var emitter : register.values()) {
                emitAppVersion(emitter, resolvedAppVersion);
            }
        } catch (final Throwable ex) {
            LOGGER.warn(() -> "Could not resolve the application version. Attempts left: [%s].".formatted(attemptsLeft - 1), ex);
            if (attemptsLeft > 1) {
                runAsync(() -> resolveAppVersion(attemptsLeft - 1), delayedExecutor(APP_VERSION_RETRY_DELAY_MILLIS, MILLISECONDS));
            }
        }
    }

    @Override
    public void deregisterEmitter(final User user, final String sseUid) {
        LOGGER.info(() -> "Deregistering event emitter for web client [%s, %s].".formatted(user, sseUid));
        // No exceptions are expected during the emitter removal and closing, but let's be defensive.
        // Because we cannot do much in such a case, we simply log the error for further analysis.
        try {
            final IEventSourceEmitter emitter = register.remove(key(user, sseUid));
            if (emitter != null) {
                emitter.close();
            }
        } catch (final Throwable ex) {
            LOGGER.error(() -> "Deregistering event emitter for web client [%s, %s] resulted in error.".formatted(user, sseUid), ex);
        } finally {
            logRegisterSize();
        }
    }

    /// A helper method to report the number of SSE connections – a distinct by user and a total number.
    ///
    private void logRegisterSize() {
        LOGGER.info(() -> {
            final var keySet = register.keySet();
            final var distinctUserConnections = keySet.stream().map(t2 -> t2._1).distinct().count();
            return "SSE connections: [%s] distinct, [%s] total.".formatted(distinctUserConnections, keySet.size());
        });
    }
    
    @Override
    public IEventSourceEmitter getEmitter(final User user, final String sseUid) {
        return register.get(key(user, sseUid));
    }

    /// Broadcasts an event to all registered emitters (i.e., clients).
    /// This method is thread-safe and could in practice get invoked by multiple threads.
    ///
    /// Iterating over emitters, which are stored in a concurrent map, is thread-safe with "weak consistency".
    /// This means that iterators obtained for [ConcurrentHashMap] can tolerate concurrent modification.
    /// And it traverses elements as they existed when an iterator was constructed.
    /// And it may (but not guaranteed to) reflect modifications to the collection after the construction of an iterator.
    ///
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

    /// Broadcasts `data` to all registered emitters (i.e., clients).
    /// This method is thread-safe and could in practice get invoked by multiple threads, as per explanation in [#event(String,String)].
    ///
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

    /// Broadcasts `comment` to all registered emitters (i.e., clients).
    /// This method is thread-safe and could in practice get invoked by multiple threads, as per explanation in [#event(String,String)].
    ///
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

    /// Removes and closes all emitters, registered previously.
    ///
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
                    LOGGER.warn("Non critical error during closing of emitters.", ex);
                }
            }
            
            LOGGER.info("Closing all emitters...");
            for (final Iterator<IEventSourceEmitter> iter = register.values().iterator(); iter.hasNext();) {
                final IEventSourceEmitter emitter = iter.next();
                iter.remove();
                try {
                    emitter.close();
                } catch (final Throwable ex) {
                    LOGGER.warn("Non critical error during closing of emitters.", ex);
                }
            }
            
        }
    }

}
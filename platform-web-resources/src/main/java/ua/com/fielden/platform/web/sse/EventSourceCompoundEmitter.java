package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@link IEventSourceEmitter} implementation that acts as a broadcast emitter (broadcaster), which passes events to all registered emitters.
 * There should be only a single instance of this class per application.
 * All emitters that get registered by this broadcaster correspond to separate web client instances.
 *
 * @author TG Team
 *
 */
public class EventSourceCompoundEmitter implements IEventSourceEmitter, IEventSourceEmitterRegister {

    private Map<String, IEventSourceEmitter> emitters = Collections.synchronizedMap(new HashMap<>());

    /**
     * A collection of event sources, specified for various Entity Centres.
     * The only reason for this collection is to prevent GC from collecting instantiated event sources, which are required for SSE eventing.
     */
    private Map<Class<? extends IEventSource>, IEventSource> eventSources = new HashMap<>();

    /**
     * Creates and registers an instance of {@code eventSourceClass}, but only if such SSE class was not instantiated before.
     * SSE classes may get specified as part of Entity Centre configurations.
     *
     * @param eventSourceClass
     * @param eventSourceSupplier
     * @return
     * @throws IOException
     */
    public EventSourceCompoundEmitter createAndRegisterEventSource(final Class<? extends IEventSource> eventSourceClass, final Supplier<IEventSource> eventSourceSupplier) throws IOException {
        eventSources.computeIfAbsent(eventSourceClass, argNotUsed -> {
            final IEventSource eventSource = eventSourceSupplier.get();
            eventSource.connect(this);
            return eventSource;});
        return this;
    }

    @Override
    public IEventSourceEmitterRegister registerEmitter(final String uid, final IEventSourceEmitter emitter) {
        emitters.put(uid, emitter);
        return this;
    }

    @Override
    public IEventSourceEmitter getEmitter(final String uid) {
        return emitters.get(uid);
    }

    @Override
    public boolean deregisterEmitter(final String uid) {
        synchronized(this) {
            if (emitters.containsKey(uid)) {
                emitters.get(uid).close();
                emitters.remove(uid);
                return true;
            }
            return false;
        }
    }

    @Override
    public void event(final String name, final String data) throws IOException {
        synchronized(this) {
            for(final IEventSourceEmitter emitter: emitters.values()) {
                emitter.event(name, data);
            }
        }
    }

    @Override
    public void data(final String data) throws IOException {
        synchronized(this) {
            for(final IEventSourceEmitter emitter: emitters.values()) {
                emitter.data(data);
            }
        }
    }

    @Override
    public void comment(final String comment) throws IOException {
        synchronized(this) {
            for(final IEventSourceEmitter emitter: emitters.values()) {
                emitter.comment(comment);
            }
        }
    }

    @Override
    public void close() {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> deregisterEmitter(uid));
        }
    }

}
package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link IEventSourceEmitter} implementation that manages a number of registered emitters. Each such emitter corresponds to a separate instance of a web client.
 * This class is also responsible for registering event sources.
 *
 * @author TG Team
 *
 */
public class EventSourceCompoundEmitter implements IEventSourceEmitter, IEventSourceEmitterRegister, IEventSourceRegister {

    private Map<String, IEventSourceEmitter> emitters = Collections.synchronizedMap(new HashMap<>());

    private Map<Class<? extends IEventSource>, IEventSource> eventSources = Collections.synchronizedMap(new HashMap<>());

    @Override
    public IEventSourceRegister registerEventSource(final IEventSource eventSource) {
        eventSources.put(eventSource.getClass(), eventSource);
        return this;
    }

    @Override
    public boolean hasEventSource(final Class<? extends IEventSource> eventSourceClass) {
        return eventSources.containsKey(eventSourceClass);
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
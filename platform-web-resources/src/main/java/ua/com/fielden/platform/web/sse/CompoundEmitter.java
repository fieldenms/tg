package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link IEmitter} implementation that manages number of registered emitters (clients). This also registers event sources in the system.
 *
 * @author TG Team
 *
 */
public class CompoundEmitter implements IEmitter, IEmitterManager, IEventSourceManager{

    private Map<String, IEmitter> emitters = Collections.synchronizedMap(new HashMap<>());

    private Map<Class<? extends IEventSource>, IEventSource> eventSources = Collections.synchronizedMap(new HashMap<>());

    @Override
    public IEventSourceManager registerEventSource(final IEventSource eventSource) {
        eventSources.put(eventSource.getClass(), eventSource);
        return this;
    }

    @Override
    public boolean hasEventSource(final Class<? extends IEventSource> eventSourceClass) {
        return eventSources.containsKey(eventSourceClass);
    }

    @Override
    public IEmitterManager registerEmitter(final String uid, final IEmitter emitter) {
        emitters.put(uid, emitter);
        return this;
    }

    @Override
    public IEmitter getEmitter(final String uid) {
        return emitters.get(uid);
    }

    @Override
    public boolean closeEmitter(final String uid) {
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
            for(final IEmitter emitter: emitters.values()) {
                emitter.event(name, data);
            }
        }
    }

    @Override
    public void data(final String data) throws IOException {
        synchronized(this) {
            for(final IEmitter emitter: emitters.values()) {
                emitter.data(data);
            }
        }
    }

    @Override
    public void comment(final String comment) throws IOException {
        synchronized(this) {
            for(final IEmitter emitter: emitters.values()) {
                emitter.comment(comment);
            }
        }
    }

    @Override
    public void close() {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> closeEmitter(uid));
        }
    }
}

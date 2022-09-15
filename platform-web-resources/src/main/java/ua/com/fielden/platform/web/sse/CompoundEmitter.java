package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompoundEmitter implements IEmitter, IEmitterManager, IEventSourceManager {

    private Map<String, IEmitter> emitters = Collections.synchronizedMap(new HashMap<>());

    private Map<Class<? extends IEventSource>, IEventSource> eventSources = Collections.synchronizedMap(new HashMap<>());

    @Override
    public IEventSourceManager registerEventSource(final IEventSource eventSource) {
        if (!eventSources.containsKey(eventSource.getClass())) {
            eventSources.put(eventSource.getClass(), eventSource);
        }
        return this;
    }

    @Override
    public boolean removeEventSource(final IEventSource eventSource) {
        return eventSources.remove(eventSource.getClass()) != null;
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
        if (emitters.containsKey(uid)) {
            emitters.get(uid).close();
            emitters.remove(uid);
            return true;
        }
        return false;
    }

    @Override
    public void event(final String name, final String data) {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> {
                try {
                    emitter.event(name, data);
                } catch (final IOException e) {
                    closeEmitter(uid);
                }
            });
        }
    }

    @Override
    public void data(final String data) throws IOException {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> {
                try {
                    emitter.data(data);
                } catch (final IOException e) {
                    closeEmitter(uid);
                }
            });
        }
    }

    @Override
    public void comment(final String comment) throws IOException {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> {
                try {
                    emitter.comment(comment);
                } catch (final IOException e) {
                    closeEmitter(uid);
                }
            });
        }
    }

    @Override
    public void close() {
        synchronized(this) {
            emitters.forEach((uid, emitter) -> closeEmitter(uid));
        }
    }
}

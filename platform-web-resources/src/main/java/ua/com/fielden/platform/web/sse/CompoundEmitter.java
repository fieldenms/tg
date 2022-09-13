package ua.com.fielden.platform.web.sse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class CompoundEmitter implements IEmitter, IEmitterManager, IEventSourceManager {

    private BiMap<String, IEmitter> emitters = Maps.synchronizedBiMap(HashBiMap.create());

    private List<IEventSource> eventSources = Collections.synchronizedList(new ArrayList<>());

    @Override
    public IEventSourceManager registerEventSource(final IEventSource eventSource) {
        eventSources.add(eventSource);
        return this;
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
    public void event(final String name, final String data){
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

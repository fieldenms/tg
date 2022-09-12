package ua.com.fielden.platform.web.sse;

import java.io.IOException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class CompoundEmitter implements IEmitter, IEmitterManager, IEventSourceManager {

    BiMap<String, IEmitter> emitters = Maps.synchronizedBiMap(HashBiMap.create());

    @Override
    public IEventSourceManager registerEventSource(final IEventSource eventSource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEmitterManager registerEmitter(final String uid, final IEmitter emitter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEmitter getEmitter(final String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean closeEmitter(final String uid) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void event(final String name, final String data) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void data(final String data) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void comment(final String comment) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}

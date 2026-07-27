package ua.com.fielden.platform.sample.domain.crit_gen;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;

import java.io.InputStream;

@Singleton
public class StubSerialiser implements ISerialiser {

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        return null;
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        return null;
    }

    @Override
    public byte[] serialise(final Object obj, final SerialiserEngines serialiserEngine) {
        return null;
    }

    @Override
    public byte[] serialise(final Object obj) {
        return null;
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) {
        return null;
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) {
        return null;
    }

    @Override
    public EntityFactory factory() {
        return null;
    }

    @Override
    public ISerialiserEngine getEngine(final SerialiserEngines serialiserEngine) {
        return null;
    }
    
}

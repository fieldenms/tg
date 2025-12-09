package ua.com.fielden.platform.serialisation.api.impl;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.serialisation.api.*;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;

import java.io.InputStream;

/**
 * The default implementation for {@link ISerialiser} with JACKSON engine.
 *
 * @author TG Team
 *
 */
@Singleton
public class Serialiser implements ISerialiser {
    private final EntityFactory factory;
    private ISerialiserEngine jacksonEngine;

    @Inject
    public Serialiser(final EntityFactory factory, final TgJackson tgJackson) {
        this.factory = factory;
        this.jacksonEngine = tgJackson;
    }
    
    public static Serialiser createSerialiserWithJackson(final EntityFactory factory, final ISerialisationClassProvider provider, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        final var tgJackson = new TgJackson(factory, provider, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);
        return new Serialiser(factory, tgJackson);
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return jacksonEngine.deserialise(content, type);
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return jacksonEngine.deserialise(content, type);
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    @Override
    public byte[] serialise(final Object obj, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return jacksonEngine.serialise(obj);
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) {
        return deserialise(content, type, SerialiserEngines.JACKSON);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) {
        return deserialise(content, type, SerialiserEngines.JACKSON);
    }

    @Override
    public byte[] serialise(final Object obj) {
        return serialise(obj, SerialiserEngines.JACKSON);
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }

    @Override
    public ISerialiserEngine getEngine(final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return jacksonEngine;
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    public ISerialiser setEngine(final SerialiserEngines serialiserEngine, final ISerialiserEngine engine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            this.jacksonEngine = engine;
        } else {
            throw new SerialisationException("Unsupported serialisation engine.");
        }
        return this;
    }
    
}

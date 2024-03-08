package ua.com.fielden.platform.serialisation.api.impl;

import java.io.InputStream;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;

/**
 * The default implementation for {@link ISerialiser} with JACKSON engine.
 *
 * @author TG Team
 *
 */
public class Serialiser implements ISerialiser {
    private final EntityFactory factory;
    private ISerialiserEngine tgJackson;
    private final ISerialisationClassProvider provider;

    @Inject
    public Serialiser(final EntityFactory factory, final ISerialisationClassProvider provider) {
        this.factory = factory;
        this.provider = provider;
    }
    
    public static Serialiser createSerialiserWithJackson(final EntityFactory factory, final ISerialisationClassProvider provider, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        final Serialiser serialiser = new Serialiser(factory, provider);
        serialiser.initJacksonEngine(serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);
        return serialiser;
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return tgJackson.deserialise(content, type); 
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return tgJackson.deserialise(content, type); 
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    @Override
    public byte[] serialise(final Object obj, final SerialiserEngines serialiserEngine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            return tgJackson.serialise(obj); 
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
            return tgJackson; 
        }
        throw new SerialisationException("Unsupported serialisation engine.");
    }

    public ISerialiser setEngine(final SerialiserEngines serialiserEngine, final ISerialiserEngine engine) {
        if (SerialiserEngines.JACKSON == serialiserEngine) {
            tgJackson = engine;
        } else {
            throw new SerialisationException("Unsupported serialisation engine.");
        }
        return this;
    }
    
    @Override
    public ISerialiser initJacksonEngine(final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        tgJackson = new TgJackson(factory, provider, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);
        return this;
    }
}

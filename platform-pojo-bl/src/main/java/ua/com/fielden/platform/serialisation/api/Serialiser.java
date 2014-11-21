package ua.com.fielden.platform.serialisation.api;

import java.io.InputStream;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.serialisation.kryo.TgKryo;

import com.google.inject.Inject;

/**
 * The default implementation for {@link ISerialiser} with two engines: KRYO (default) and JACKSON.
 *
 * @author TG Team
 *
 */
public class Serialiser implements ISerialiser {
    private final EntityFactory factory;
    private ISerialiserEngine tgKryo;
    private ISerialiserEngine tgObjectMapper;

    @Inject
    public Serialiser(final EntityFactory factory, final ISerialisationClassProvider provider) {
        this.factory = factory;
        createTgKryo(factory, provider); // the serialiser engine will be set automatically
        this.tgObjectMapper = new TgObjectMapper(factory, provider);
    }

    protected ISerialiserEngine createTgKryo(final EntityFactory factory, final ISerialisationClassProvider provider) {
        return new TgKryo(factory, provider, this);
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type, final SerialiserEngines serialiserEngine) throws Exception {
        return SerialiserEngines.KRYO.equals(serialiserEngine) ? tgKryo.deserialise(content, type) : tgObjectMapper.deserialise(content, type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type, final SerialiserEngines serialiserEngine) throws Exception {
        return SerialiserEngines.KRYO.equals(serialiserEngine) ? tgKryo.deserialise(content, type) : tgObjectMapper.deserialise(content, type);
    }

    @Override
    public byte[] serialise(final Object obj, final SerialiserEngines serialiserEngine) {
        return SerialiserEngines.KRYO.equals(serialiserEngine) ? tgKryo.serialise(obj) : tgObjectMapper.serialise(obj);
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) throws Exception {
        return deserialise(content, type, SerialiserEngines.KRYO);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) throws Exception {
        return deserialise(content, type, SerialiserEngines.KRYO);
    }

    @Override
    public byte[] serialise(final Object obj) {
        return serialise(obj, SerialiserEngines.KRYO);
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }

    @Override
    public ISerialiserEngine getEngine(final SerialiserEngines serialiserEngine) {
        return SerialiserEngines.KRYO.equals(serialiserEngine) ? tgKryo : tgObjectMapper;
    }

    public ISerialiser setEngine(final SerialiserEngines serialiserEngine, final ISerialiserEngine engine) {
        if (SerialiserEngines.KRYO.equals(serialiserEngine)) {
            tgKryo = engine;
        } else {
            tgObjectMapper = engine;
        }
        return this;
    }
}

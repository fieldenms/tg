package ua.com.fielden.platform.serialisation.api;

import java.io.InputStream;

import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;

/**
 * API for serialising and deserialising entities, queries and other TG objects. There are multiple {@link SerialiserEngines} to be used for serialisation / deserialisation.
 *
 * @author TG Team
 *
 */
public interface ISerialiser extends ISerialiserEngine {

    /**
     * Serialises an object using specified {@link SerialiserEngines}.
     *
     * @param obj
     * @param serialiserEngine
     *
     * @return - a byte array for serialised object
     */
    byte[] serialise(final Object obj, final SerialiserEngines serialiserEngine);

    /**
     * Deserialises an object using specified {@link SerialiserEngines}.
     *
     * @param content -- byte array of the serialised object
     * @param type -- the type of the deserialised object, which should be known when deserialisation happens
     * @param serialiserEngine
     * @return
     * @throws Exception
     */
    <T> T deserialise(final byte[] content, final Class<T> type, final SerialiserEngines serialiserEngine);

    /**
     * Deserialises an object using specified {@link SerialiserEngines}.
     *
     * @param content -- input stream of the serialised object
     * @param type -- the type of the deserialised object, which should be known when deserialisation happens
     * @param serialiserEngine
     * @return
     * @throws Exception
     */
    <T> T deserialise(final InputStream content, final Class<T> type, final SerialiserEngines serialiserEngine);

    /**
     * Returns serialisation engine by its name.
     *
     * @param serialiserEngine
     *
     * @return
     */
    ISerialiserEngine getEngine(final SerialiserEngines serialiserEngine);

    ISerialiser initJacksonEngine(final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache);
}

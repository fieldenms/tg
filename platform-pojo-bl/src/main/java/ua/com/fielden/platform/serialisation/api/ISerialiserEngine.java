package ua.com.fielden.platform.serialisation.api;

import java.io.InputStream;

import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * API for serialising and deserialising entities and entity queries.
 *
 * @author TG Team
 *
 */
public interface ISerialiserEngine {
    /**
     * Serialises an object.
     *
     * @param obj
     *
     * @return - a byte array for serialised object
     */
    byte[] serialise(final Object obj);

    /**
     * Deserialises an object.
     *
     * @param content
     *            - a byte array of the serialised object
     * @param type
     *            - the type of the deserialised object, which should be known when deserialisation happens
     * @return
     * @throws Exception
     */
    <T> T deserialise(final byte[] content, final Class<T> type);

    /**
     * Deserialises an object.
     *
     * @param content
     *            - an input stream of the serialised object
     * @param type
     *            - the type of the deserialised object, which should be known when deserialisation happens
     * @return
     * @throws Exception
     */
    <T> T deserialise(final InputStream content, final Class<T> type);

    EntityFactory factory();
}

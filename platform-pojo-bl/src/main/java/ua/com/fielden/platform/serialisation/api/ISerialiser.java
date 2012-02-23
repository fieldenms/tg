package ua.com.fielden.platform.serialisation.api;

import java.io.InputStream;

import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * API for serialising and deserialising entities and entity queries.
 *
 * @author TG Team
 *
 */
public interface ISerialiser {

    byte[] serialise(final Object obj);

    <T> T deserialise(final byte[] content, Class<T> type) throws Exception;

    <T> T deserialise(final InputStream content, Class<T> type) throws Exception;

    EntityFactory factory();

}

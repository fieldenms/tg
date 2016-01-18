package ua.com.fielden.platform.entity;

import java.io.InputStream;

/**
 * A base class for entities that are associated with input stream processing such as processing of files uploaded to the server.
 * Entities of this type have field (not a property to avoid accidental serialisation and what not) of type {@link InputStream} that should be assigned
 * and processed by a corresponding companion.
 * 
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractEntityWithInputStream<K extends Comparable<?>> extends AbstractEntity<K> {
    private static final long serialVersionUID = 1L;

    public static final String IS = "inputStream";

    private InputStream inputStream;

    public AbstractEntityWithInputStream<K> setInputStream(InputStream is) {
        this.inputStream = is;
        return this;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
    
}

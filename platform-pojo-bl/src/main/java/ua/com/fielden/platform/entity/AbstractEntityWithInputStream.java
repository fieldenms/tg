package ua.com.fielden.platform.entity;

import java.io.InputStream;
import java.util.Optional;

import ua.com.fielden.platform.rx.AbstractSubjectKind;

/**
 * A base class for entities that are associated with input stream processing such as processing of files uploaded to the server.
 * Entities of this type have a field (not a property to avoid accidental serialisation and what not) of type {@link InputStream} that should be assigned
 * and processed by a corresponding companion.
 * <p>
 * This class also provides access to an externally assigned event source subject that could be used by the processing logic at the companion level to report processing progress.
 * 
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractEntityWithInputStream<K extends Comparable<?>> extends AbstractEntity<K> {

    private InputStream inputStream;
    private String origFileName;
    
    private Optional<AbstractSubjectKind<Integer>> eventSourceSubject = Optional.empty();

    public AbstractEntityWithInputStream<K> setOrigFileName(final String origFileName) {
        this.origFileName = origFileName;
        return this;
    }

    public String getOrigFileName() {
        return origFileName;
    }
    
    public AbstractEntityWithInputStream<K> setInputStream(final InputStream is) {
        this.inputStream = is;
        return this;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

    public Optional<AbstractSubjectKind<Integer>> getEventSourceSubject() {
        return eventSourceSubject;
    }

    public AbstractEntityWithInputStream<K> setEventSourceSubject(final AbstractSubjectKind<Integer> eventSourceSubject) {
        this.eventSourceSubject = Optional.of(eventSourceSubject);
        return this;
    }
    
}

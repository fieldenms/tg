package ua.com.fielden.platform.dao;

/**
 * Runtime exception that should be thrown from within {@link AbstractFunctionalEntityForCollectionModificationProducer} implementation (and also in other logic pertaining collection
 * modification).
 * 
 * @author TG Team
 *
 */
public class CollectionModificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CollectionModificationException(final String msg) {
        super(msg);
    }
    
    public CollectionModificationException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
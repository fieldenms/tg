package ua.com.fielden.platform.dao.exceptions;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

/**
 * The exception type to be thrown in companion {@link IEntityDao#save(ua.com.fielden.platform.entity.AbstractEntity)} method in case where continuation (functional entity) is
 * missing.
 * 
 * @author TG Team
 *
 */
public class ContinuationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public final String continuationTypeStr;
    public final String continuationProperty;

    /**
     * Creates continuation exception based on <code>continuationType</code>.
     * 
     * @param continuationType -- functional entity type that represents continuation
     * @param continuationProperty -- the property on companion object into which continuation will arrive
     */
    public ContinuationException(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> continuationType, final String continuationProperty) {
        super("Continuation for [" + continuationType.getSimpleName() + "] entity and property [" + continuationProperty + "].");
        this.continuationTypeStr = continuationType.getName();
        this.continuationProperty = continuationProperty;
    }
}

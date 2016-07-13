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

    /**
     * Creates continuation exception based on <code>continuationType</code>.
     * 
     * @param continuationType -- functional entity type that represents continuation
     */
    public ContinuationException(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> continuationType) {
        super("Continuation for [" + continuationType.getSimpleName() + "] entity.");
        this.continuationTypeStr = continuationType.getSimpleName();
    }
}

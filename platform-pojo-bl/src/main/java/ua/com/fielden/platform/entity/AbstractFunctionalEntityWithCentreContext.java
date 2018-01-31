package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A base class for functional entities that are intended to be used on entity centres / masters.
 * <p>
 * The main difference with {@link AbstractEntity} is that it gets <code>context</code> of type {@link CentreContext} during producing phase,
 * which represents an execution context as provided when a functional entity gets actioned.
 * <p>
 * Entity companion is capable of taking advantage of the provided context information for functional entity computations.
 * This information should be computed and gathered into the functional entity inside producer.
 *
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractFunctionalEntityWithCentreContext<K extends Comparable<?>> extends AbstractEntity<K> {
    
    /**
     * Private context to be used in {@link IContextDecomposer} API.
     */
    private CentreContext<?, ?> context;
    
    CentreContext<?, ?> context() {
        return context;
    }
    
    void setContext(final CentreContext<?, ?> context) {
        this.context = context;
    }
    
}

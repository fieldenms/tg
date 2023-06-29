package ua.com.fielden.platform.web.centre.api.context;

import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A contract that provide ability to extended this context with context of insertion point if this is a context for the action on entity centre. Also it allows one to set computation function.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IExtendedEntityCentreContextWithFunctionSelector<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorDone<T> {

    /**
     * Extends this context with insertion point context defined by specified context configuration object.
     *
     * @param insertionPointFunctionalType
     * @param contextForInsertionPoint
     * @return
     */
    IExtendedEntityCentreContextWithFunctionSelector<T> extendWithInsertionPointContext(Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, CentreContextConfig contextForInsertionPoint);

    /**
     * Sets the computation function for this context.
     *
     * @param computation
     * @return
     */
    IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation);
}

package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

/**
 * A contract that defines what action should be chosen for specific entity.
 *
 * @author TG Team
 *
 */
public interface IEntityMultiActionSelector {

    /**
     * Returns the type of functional entity that should be associated with specified entity
     *
     * @param entity
     * @return
     */
    Class<? extends AbstractFunctionalEntityWithCentreContext<?>> getActionFor(final AbstractEntity<?> entity);
}

package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that defines what action should be chosen for specific entity.
 *
 * @author TG Team
 *
 */
public interface IEntityMultiActionSelector {

    /**
     * Returns the index of action from the list of available ones in multiple action configuration object, that should be associated with specified entity.
     *
     * @param entity
     * @return index of action in the list of actions of multiple action configuration object.
     */
    int getActionFor(final AbstractEntity<?> entity);
}

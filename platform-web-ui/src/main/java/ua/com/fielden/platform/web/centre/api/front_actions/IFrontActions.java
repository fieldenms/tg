package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * A contract for front action that will be displayed in selection criteria view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IFrontActions<T extends AbstractEntity<?>>{

    /**
     * Augments action to front actions.
     *
     * @param actionConfig
     * @return
     */
    IAlsoFrontActions<T> addFrontAction(final EntityActionConfig actionConfig);
}

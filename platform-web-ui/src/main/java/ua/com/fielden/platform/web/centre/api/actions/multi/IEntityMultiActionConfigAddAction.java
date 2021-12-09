package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * A contract to add specific action to multi-action configuration object.
 *
 * @author TG Team
 *
 */
public interface IEntityMultiActionConfigAddAction {

    /**
     * Adds specified action to multi-action configuration object.
     *
     * @param action
     * @return
     */
    IEntityMultiActionConfigBuild addAction(EntityActionConfig action);
}

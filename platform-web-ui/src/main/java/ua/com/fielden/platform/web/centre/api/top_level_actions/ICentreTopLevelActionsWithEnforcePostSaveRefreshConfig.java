package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.front_actions.IFrontWithTopActions;

/**
 * A contract to force centre to refresh the current page upon successfully saving a related entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T extends AbstractEntity<?>> extends IFrontWithTopActions<T> {

    /**
     * Forces centre to refresh the current page post successful save of any related entity.
     *
     * @return
     */
    IFrontWithTopActions<T> enforcePostSaveRefresh();

}

package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to force centre to refresh the current page upon successfully saving a related entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T> {

    /**
     * Forces centre to refresh the current page post successful save of any related entity.
     *
     * @return
     */
    ICentreTopLevelActions<T> enforcePostSaveRefresh();

}

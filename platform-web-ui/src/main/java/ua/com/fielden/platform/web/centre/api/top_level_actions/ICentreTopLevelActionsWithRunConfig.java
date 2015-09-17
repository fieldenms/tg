package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to force centre run automatically after load.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithRunConfig<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T> {

    /**
     * Forces centre to run automatically after it was loaded.
     *
     * @return
     */
    ICentreTopLevelActions<T> runAutomatically();

}

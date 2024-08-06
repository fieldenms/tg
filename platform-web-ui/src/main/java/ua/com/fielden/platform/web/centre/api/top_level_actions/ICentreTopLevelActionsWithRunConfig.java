package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.RunAutomaticallyOptions;

/**
 * A contract to force centre run automatically after load.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActionsWithRunConfig<T extends AbstractEntity<?>> extends ICentreTopLevelActionsWithSse<T> {

    /**
     * Forces centre to run automatically after it was loaded.
     *
     * @return
     */
	ICentreTopLevelActionsWithSse<T> runAutomatically();

    /**
     * Forces centre to run automatically after it was loaded. Some {@link RunAutomaticallyOptions} may be applied.
     *
     * @return
     */
    ICentreTopLevelActionsWithSse<T> runAutomatically(final RunAutomaticallyOptions option, final RunAutomaticallyOptions... additionalOptions);

}

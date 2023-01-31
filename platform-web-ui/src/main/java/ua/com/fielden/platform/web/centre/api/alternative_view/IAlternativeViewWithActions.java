package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * A contract to specify functional actions for alternative view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeViewWithActions<T extends AbstractEntity<?>> {

    /**
     * Adds new top functional action the the alternative view.
     *
     * @param actionConfig
     * @return
     */
    IAlternativeViewAlso<T> addTopAction(final EntityActionConfig actionConfig);
}

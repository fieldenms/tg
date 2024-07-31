package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * The contract for defining entity centre alternative view, which is driven by functional entities.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeView<T extends AbstractEntity<?>> extends IEcbCompletion<T> {

    /**
     * Adds new alternative view and associates it with the specified action.
     *
     * @param actionConfig
     * @return
     */
    IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig);
}

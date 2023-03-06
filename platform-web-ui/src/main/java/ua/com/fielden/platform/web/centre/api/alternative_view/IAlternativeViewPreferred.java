package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify the preferred alternative view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeViewPreferred<T extends AbstractEntity<?>> extends IAlternativeViewWithActionsExtended<T> {

    /**
     * Makes this alternative view as preferred.
     *
     * @return
     */
    IAlternativeViewWithActionsExtended<T> makePreferred();
}

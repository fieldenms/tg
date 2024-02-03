package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify new top functional action or toolbar.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeViewWithActionsExtended<T extends AbstractEntity<?>> extends IAlternativeViewWithToolbar<T>, IAlternativeViewWithActions<T> {

}

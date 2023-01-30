package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 * A contract to specify toolbar for alternative view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeViewWithToolbar<T extends AbstractEntity<?>> extends IAlternativeView<T> {

    /**
     * Specify toolbar configuration for this alternative view.
     *
     * @param toolbar
     * @return
     */
    IAlternativeView<T> setToolbar(final IToolbarConfig toolbar);
}

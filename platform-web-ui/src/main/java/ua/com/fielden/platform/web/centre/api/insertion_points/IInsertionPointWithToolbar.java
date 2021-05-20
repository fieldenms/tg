package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 * A contract for insertion point with toolbar.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointWithToolbar<T extends AbstractEntity<?>> extends IInsertionPointsFlexible<T> {

    /**
     * Specify tool bar configuration for this insertion point.
     *
     * @param toolbar
     * @return
     */
    IInsertionPointsFlexible<T> setToolbar(IToolbarConfig toolbar);
}

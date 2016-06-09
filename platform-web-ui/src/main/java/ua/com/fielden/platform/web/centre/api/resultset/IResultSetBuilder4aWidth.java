package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;

/**
 * Provides the convenient way to specify rigid or flexible width for column in result set.
 *
 * @author TG Team
 *
 */
public interface IResultSetBuilder4aWidth<T extends AbstractEntity<?>> extends IWithTooltip<T> {

    /**
     * Specifies the rigid column width. The width of the column won't change when the size of grid changes.
     *
     * @param width
     * @return
     */
    IWithTooltip<T> width(int width);

    /**
     * Specifies the flexible column width that will change when the width of grid changes.
     *
     * @param minWidth
     * @return
     */
    IWithTooltip<T> minWidth(int minWidth);
}

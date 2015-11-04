package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;

/**
 * This contract provides a way for specifying result set ordering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder4OrderingDirection<T extends AbstractEntity<?>> {


    IWithTooltip<T> desc();
    IWithTooltip<T> asc();
}

package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder;

/**
 * A contract for selecting a multi-valued, single-valued or range kind of selection criteria.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISelectionCritKindSelector<T extends AbstractEntity<?>> extends IResultSetBuilder<T> {
    IMultiValueCritSelector<T> asMulti();
    ISingleValueCritBuilder<T> asSingle();
    IRangeValueCritBuilder<T> asRange();
}

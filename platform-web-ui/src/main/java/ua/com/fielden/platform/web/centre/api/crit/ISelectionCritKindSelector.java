package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for selecting a multi-valued, single-valued or range kind of selection criteria.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISelectionCritKindSelector<T extends AbstractEntity<?>> {
    IMultiValueCritSelector<T> asMulti();
    ISingleValueCritSelector<T> asSingle();
    IRangeValueCritSelector<T> asRange();
}

package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This contract provides a way for specifying result set ordering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder4OrderingDirection<T extends AbstractEntity<?>> {


    IResultSetBuilder4aWidth<T> desc();

    IResultSetBuilder4aWidth<T> asc();
}

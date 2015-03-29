package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This contract provides a way for specifying result set ordering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1OrderingDirection<T extends AbstractEntity<?>> {


    IResultSetBuilder2WithPropAction<T> desc();
    IResultSetBuilder2WithPropAction<T> asc();
}

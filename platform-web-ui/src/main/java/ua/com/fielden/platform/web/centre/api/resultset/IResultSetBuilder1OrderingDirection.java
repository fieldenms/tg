package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;

/**
 * This contract provides a way for specifying result set ordering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1OrderingDirection<T extends AbstractEntity<?>> {


    IWithSummary<T> desc();
    IWithSummary<T> asc();
}

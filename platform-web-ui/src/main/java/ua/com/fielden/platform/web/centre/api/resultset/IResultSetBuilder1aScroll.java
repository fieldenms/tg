package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;

/**
 *
 * Provides a convenient abstraction for making EGI not scrollable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1aScroll<T extends AbstractEntity<?>> extends IResultSetBuilder1bPageCapacity<T> {

    IResultSetBuilder1bPageCapacity<T> notScrollable();

    IResultSetBuilder1bPageCapacity<T> withScrollingConfig(IScrollConfig scrollConfig);
}

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
public interface IResultSetBuilder1dScroll<T extends AbstractEntity<?>> extends IResultSetBuilder1dCentreScroll<T> {

    IResultSetBuilder1dCentreScroll<T> notScrollable();

    IResultSetBuilder1dCentreScroll<T> withScrollingConfig(IScrollConfig scrollConfig);
}

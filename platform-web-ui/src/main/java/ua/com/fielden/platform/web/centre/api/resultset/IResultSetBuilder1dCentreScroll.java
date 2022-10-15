package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a convenient abstraction to configure centre scroll. Whether it should all centre result view or just separate insertion point containers.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1dCentreScroll<T extends AbstractEntity<?>> extends IResultSetBuilder1eDraggable<T> {

    /**
     * Locks scrolling to centre instead of separate insertion point containers
     *
     * @return
     */
    IResultSetBuilder1eDraggable<T> lockScrollingForInsertionPoints();
}

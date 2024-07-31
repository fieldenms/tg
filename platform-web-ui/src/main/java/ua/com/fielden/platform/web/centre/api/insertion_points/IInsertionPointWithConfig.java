package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IWithLeftSplitterPosition;

/**
 * Aggregates Insertion points and their configurations. That was made to disallow configuring splitter and custom layout before any insertion point was added.
 *
 * @param <T>
 */
public interface IInsertionPointWithConfig<T extends AbstractEntity<?>> extends IInsertionPoints<T>, IWithLeftSplitterPosition<T> {
}

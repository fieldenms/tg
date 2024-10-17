package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IWithLeftSplitterPosition;

/**
 * A contract that aggregates insertion points and their configurations.
 * The intention is to disallow configuring splitters / custom layout before any insertion point was added.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointWithConfig<T extends AbstractEntity<?>> extends IInsertionPoints<T>, IWithLeftSplitterPosition<T> {
}
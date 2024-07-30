package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IWithLeftSplitterPosition;

public interface IInsertionPointWithConfig<T extends AbstractEntity<?>> extends IInsertionPoints<T>, IWithLeftSplitterPosition<T> {
}

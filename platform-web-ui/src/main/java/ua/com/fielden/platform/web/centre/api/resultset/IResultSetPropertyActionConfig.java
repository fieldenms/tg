package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public interface IResultSetPropertyActionConfig<T extends AbstractEntity<?>> extends IResultSetBuilder3Ordering<T>{

    IResultSetBuilder3Ordering<T> withEditorAction(final EntityActionConfig actionConfig);
}

package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetEditorConfig<T extends AbstractEntity<?>> extends IResultSetPropSkipValidation<IResultSetBuilder3Ordering<T>>, IResultSetBuilder3Ordering<T> {

}

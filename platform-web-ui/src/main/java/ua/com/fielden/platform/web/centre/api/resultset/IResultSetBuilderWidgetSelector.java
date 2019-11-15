package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetBuilderWidgetSelector<T extends AbstractEntity<?>> extends IResultSetEditorConfig<T>{

    IResultSetAutocompleterConfig<T> asAutocompleter();
}

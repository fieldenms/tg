package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetBuilderWidgetSelector<T extends AbstractEntity<?>> {

    IResultSetAutocompleterConfig<T> asAutocompleter();
    IResultSetBuilder3Ordering<T> asSinglelineText();
    IResultSetBuilder3Ordering<T> asMultilineText();
}

package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetAutocompleterConfig<T extends AbstractEntity<?>> extends IResultSetPropSkipValidation<IResultSetAutocompleterWithMatcher<T>>, IResultSetAutocompleterWithMatcher<T> {

}

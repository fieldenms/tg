package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetAutocompleterConfig<T extends AbstractEntity<?>> extends IResultSetBuilder3Ordering<T>{
    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IResultSetBuilder3Ordering<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType);
}

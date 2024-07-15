package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetAutocompleterWithMatcher<T extends AbstractEntity<?>> extends IResultSetAutocompleterConfigAdditionalProps<T>{

    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IResultSetAutocompleterConfigAdditionalProps<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType);
}

package ua.com.fielden.platform.web.master.api.widgets.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;

public interface IAutocompleterConfig0<T extends AbstractEntity<?>> extends IAutocompleterConfig1<T> {
    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    @SuppressWarnings("rawtypes")
    IAutocompleterConfig1<T> withMatcher(final Class<IValueMatcher> matcherType);
}
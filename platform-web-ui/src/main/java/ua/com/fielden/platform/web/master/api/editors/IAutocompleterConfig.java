package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;

/**
 * Provides an API for configuring an autocompleter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutocompleterConfig<T extends AbstractEntity<?>> extends IAlso<T> {

    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IAlso<T> withMatcher(final Class<IValueMatcher> matcherType);
}

package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

/**
 * Provides an API for configuring an autocompleter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutocompleterConfig<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {

    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IAutocompleterConfig0<T> withMatcher(final Class<IValueMatcher> matcherType);

    /**
     * An internal type to better control the Simple Master DSL. Specifically, to prevent repetitive calls to method <code>withMatcher</code>.
     *
     * @author 01es
     *
     * @param <T>
     */
    public interface IAutocompleterConfig0<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {

    }
}

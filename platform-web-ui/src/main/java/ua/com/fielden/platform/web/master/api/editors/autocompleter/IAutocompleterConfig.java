package ua.com.fielden.platform.web.master.api.editors.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Provides an API for configuring an autocompleter.
 *
 * At this stage customisation is related to
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutocompleterConfig<T extends AbstractEntity<?>> extends IAutocompleterConfig0<T> {

    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IAutocompleterConfig0<T> withMatcher(final Class<IValueMatcher> matcherType);
}

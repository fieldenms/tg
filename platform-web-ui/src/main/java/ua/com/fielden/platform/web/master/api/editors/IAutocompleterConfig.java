package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.editors.autocompleter.IAutocompleterConfig0;

/**
 * A configuration for an autocompleter that gets associated with a property of an entity type.
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
    @SuppressWarnings("rawtypes")
    IAutocompleterConfig0<T> withMatcher(final Class<IValueMatcher> matcherType);
}

package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.MatcherOptions;

public interface IAutocompleterConfig0<T extends AbstractEntity<?>> extends IAutocompleterConfig1<T> {

    /**
     * A method to provide custom value matcher type.
     *
     * @param matcher
     * @return
     */
    IAutocompleterConfig1<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType);

    /**
     * A method to provide custom value matcher type with options.
     *
     * @param matcher
     * @return
     */
    IAutocompleterConfig1<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType, final MatcherOptions option, final MatcherOptions... additionalOptions);

}
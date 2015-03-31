package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;


/**
 *
 * A contract for specifying a custom value matcher for a multi-valued selection criteria as an autocompleter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMutliValueAutocompleterBuilder<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType);
    IAlsoCrit<T> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType, final CentreContextConfig context);
}

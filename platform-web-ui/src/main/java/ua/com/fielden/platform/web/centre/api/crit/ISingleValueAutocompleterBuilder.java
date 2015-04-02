package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleEntityDefaultValueAssigner;


/**
 *
 * A contract for specifying a custom value matcher for a single-valued selection criteria as an autocompleter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleValueAutocompleterBuilder<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends ISingleEntityDefaultValueAssigner<T, V> {
    ISingleEntityDefaultValueAssigner<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType);
    ISingleEntityDefaultValueAssigner<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType, final CentreContextConfig context);
}

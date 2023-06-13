package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector;

/**
 * A contract for specifying a custom value matcher for a single-valued selection criteria as an autocompleter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleValueAutocompleterBuilder<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends ISingleValueAutocompleterBuilder1<T, V> {

    /**
     * Specifies a custom value matcher for a single-valued selection criteria. Use this method if no context is required (i.e. no selection criteria entity, master entity etc. required).
     * 
     * @param matcherType
     * @return
     */
    ISingleValueAutocompleterBuilder1<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType);

    /**
     * Specifies a custom value matcher for a single-valued selection criteria. Use this method if some specific context configuration is required (i.e. selection criteria entity, master entity etc.).
     * 
     * @param matcherType
     * @param context -- context configuration; use {@link EntityCentreContextSelector#context()} method to define it
     * @return
     */
    ISingleValueAutocompleterBuilder1<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType, final CentreContextConfig context);

    /**
     * Specifies a custom value matcher for a single-valued selection criteria with the use of {@link MatcherOptions}. Use this method if no context is required (i.e. no selection criteria entity, master entity etc. required).
     * 
     * @param matcherType
     * @param option
     * @param additionalOptions
     * @return
     */
    ISingleValueAutocompleterBuilder1<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType, final MatcherOptions option, final MatcherOptions... additionalOptions);

    /**
     * Specifies a custom value matcher for a single-valued selection criteria with the use of {@link MatcherOptions}. Use this method if some specific context configuration is required (i.e. selection criteria entity, master entity etc.).
     * 
     * @param matcherType
     * @param context -- context configuration; use {@link EntityCentreContextSelector#context()} method to define it
     * @param option
     * @param additionalOptions
     * @return
     */
    ISingleValueAutocompleterBuilder1<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType, final CentreContextConfig context, final MatcherOptions option, final MatcherOptions... additionalOptions);

}
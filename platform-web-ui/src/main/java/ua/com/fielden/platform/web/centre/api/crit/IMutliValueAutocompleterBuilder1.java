package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;


/**
 *
 * A contract for specifying properties to be displayed as part of the autocompletion list of matching values.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMutliValueAutocompleterBuilder1<T extends AbstractEntity<?>> extends IMultiStringDefaultValueAssigner<T> {

    /** Indicates whether description should also be used to highlighting the matched parts.
     *  This method does not effect whether or not the search actually involves matching be description. */
    IMultiStringDefaultValueAssigner<T> lightDesc();

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompled list of matched values.
     * Properties are specified in pairs <code>(name, highlightOption)</code> where the <code>highlightOption</code> indicates whether a corresponding property
     * should be used for highlighting of the matched parts.
     * <p>
     * If this method is used it overrides the default inclusion of property <code>desc</code>.
     * Therefore, it is required to include <code>desc</code> in the list of properties if it should also be displayed.
     *
     * @param propNameAndLightOption
     * @param morePropNameAndLightOption
     * @return
     */
    @SuppressWarnings("unchecked")
    IMultiStringDefaultValueAssigner<T> withProps(final Pair<String, Boolean> propNameAndLightOption, final Pair<String, Boolean>... morePropNameAndLightOption);
}

package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;

import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.Pair.pair;

/**
 *
 * A contract for specifying properties to be displayed as part of the autocompletion list of matching values.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultiValueAutocompleterBuilder1<T extends AbstractEntity<?>> extends IMultiStringDefaultValueAssigner<T> {

    /** Indicates whether description should also be used to highlighting the matched parts.
     *  This method does not effect whether or not the search actually involves matching be description. */
    IMultiStringDefaultValueAssigner<T> lightDesc();

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompleted list of matched values.
     * Properties are specified in pairs <code>(name, highlightOption)</code> where the <code>highlightOption</code> indicates whether a corresponding property
     * should be used for highlighting of the matched parts.
     * <p>
     * If this method is used it overrides the default inclusion of property <code>desc</code>.
     * Therefore, it is required to include <code>desc</code> in the list of properties if it should also be displayed.
     */
    @SuppressWarnings("unchecked")
    IMultiStringDefaultValueAssigner<T> withProps(final Pair<? extends CharSequence, Boolean> propNameAndLightOption, final Pair<? extends CharSequence, Boolean>... morePropNameAndLightOption);

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompleted list of matched values.
     * Properties are specified in pairs <code>(name, highlightOption)</code> where the <code>highlightOption</code> indicates whether a corresponding property
     * should be used for highlighting of the matched parts.
     * <p>
     * If this method is used it overrides the default inclusion of property <code>desc</code>.
     * Therefore, it is required to include <code>desc</code> in the list of properties if it should also be displayed.
     */
    @SuppressWarnings("unchecked")
    default IMultiStringDefaultValueAssigner<T> withProps(final T2<? extends CharSequence, Boolean> propNameAndLightOption, final T2<? extends CharSequence, Boolean>... morePropNameAndLightOption) {
        return withProps(
                pair(propNameAndLightOption._1, propNameAndLightOption._2),
                Stream.of(morePropNameAndLightOption).map(t2 -> t2.map(Pair::pair)).toArray(Pair[]::new)
        );
    }


}

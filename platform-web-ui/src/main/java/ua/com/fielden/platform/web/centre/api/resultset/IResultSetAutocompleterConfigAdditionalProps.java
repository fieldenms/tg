package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.Pair.pair;

public interface IResultSetAutocompleterConfigAdditionalProps<T extends AbstractEntity<?>> extends IResultSetBuilder3Ordering<T>{

    /** Indicates whether description should also be used to highlighting the matched parts.
     *  This method does not effect whether or not the search actually involves matching by description. */
    IResultSetBuilder3Ordering<T> lightDesc();

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompleted list of matched values.
     * Properties are specified in pairs <code>(name, highlightOption)</code> where the <code>highlightOption</code> indicates whether a corresponding property
     * should be used for highlighting of the matched parts.
     * <p>
     * If this method is used it overrides the default inclusion of property <code>desc</code>.
     * Therefore, it is required to include <code>desc</code> in the list of properties if it should also be displayed.
     */
    @SuppressWarnings("unchecked")
    IResultSetBuilder3Ordering<T> withProps(final Pair<? extends CharSequence, Boolean> propNameAndLightOption, final Pair<? extends CharSequence, Boolean>... morePropNameAndLightOption);

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompleted list of matched values.
     * Properties are specified in pairs <code>(name, highlightOption)</code> where the <code>highlightOption</code> indicates whether a corresponding property
     * should be used for highlighting of the matched parts.
     * <p>
     * If this method is used it overrides the default inclusion of property <code>desc</code>.
     * Therefore, it is required to include <code>desc</code> in the list of properties if it should also be displayed.
     */
    @SuppressWarnings("unchecked")
    default IResultSetBuilder3Ordering<T> withProps(final T2<? extends CharSequence, Boolean> propNameAndLightOption, final T2<? extends CharSequence, Boolean>... morePropNameAndLightOption) {
        return withProps(
                pair(propNameAndLightOption._1, propNameAndLightOption._2),
                Stream.of(morePropNameAndLightOption).map(t2 -> t2.map(Pair::pair)).toArray(Pair[]::new)
        );
    }

}

package ua.com.fielden.platform.web.centre.api.resultset;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.stream.Stream;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

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
     *
     * @param propNameAndLightOption
     * @param morePropNameAndLightOption
     * @return
     */
    @SuppressWarnings("unchecked")
    IResultSetBuilder3Ordering<T> withProps(final Pair<String, Boolean> propNameAndLightOption, final Pair<String, Boolean>... morePropNameAndLightOption);

    /**
     * Method to specify a complete set of properties to be displayed as part of the autocompleted list of matched values.
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
    default IResultSetBuilder3Ordering<T> withProps(final T2<IConvertableToPath, Boolean> propNameAndLightOption, final T2<IConvertableToPath, Boolean>... morePropNameAndLightOption) {
        return withProps(
            pair(propNameAndLightOption._1.toPath(), propNameAndLightOption._2),
            Stream.of(morePropNameAndLightOption).map(t2 -> pair(t2._1.toPath(), t2._2)).toArray(Pair[]::new)
        );
    }

}
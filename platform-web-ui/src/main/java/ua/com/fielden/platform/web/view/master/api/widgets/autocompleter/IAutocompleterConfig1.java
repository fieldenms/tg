package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.Pair.pair;

public interface IAutocompleterConfig1<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {

    /** Indicates whether description should also be used to highlighting the matched parts.
     *  This method does not effect whether or not the search actually involves matching be description. */
    IAutocompleterConfig2<T> lightDesc();

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
    IAutocompleterConfig2<T> withProps(final Pair<? extends CharSequence, Boolean> propNameAndLightOption, final Pair<? extends CharSequence, Boolean>... morePropNameAndLightOption);

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
    default IAutocompleterConfig2<T> withProps(final T2<? extends CharSequence, Boolean> propNameAndLightOption, final T2<? extends CharSequence, Boolean>... morePropNameAndLightOption) {
        return withProps(
                pair(propNameAndLightOption._1, propNameAndLightOption._2),
                Stream.of(morePropNameAndLightOption).map(t2 -> t2.map(Pair::pair)).toArray(Pair[]::new)
        );
    }


}

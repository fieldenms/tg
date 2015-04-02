package ua.com.fielden.platform.web.centre.api.crit.defaults;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.ISingleValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;

/**
 * A contract to provide a default value assigner or specific default values for a single-valued kind selection criteria of the integer type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleIntegerDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {

    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>> assigner);

    IAlsoCrit<T> setDefaultValue(final SingleCritOtherValueMnemonic<Integer> value);
}
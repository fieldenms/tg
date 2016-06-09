package ua.com.fielden.platform.web.centre.api.crit.defaults;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;

/**
 * A contract to provide a default value assigner or specific default values for a range kind selection criteria of an integer type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeIntegerDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>> assigner);
    IAlsoCrit<T> setDefaultValue(final RangeCritOtherValueMnemonic<Integer> value);
}
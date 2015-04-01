package ua.com.fielden.platform.web.centre.api.crit.default_assigner;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.default_value.IRangeValueAssigner;

/**
 * A contract to provide a default value assigner or specific default values for a range kind selection criteria of an integer type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeIntegerDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<Integer, T>> assigner);
    IAlsoCrit<T> setFromDefaultValue(final Integer value);
    IAlsoCrit<T> setToDefaultValue(final Integer value);
    IAlsoCrit<T> setFromAndToDefaultValues(final Integer fromValue, final Integer toValue);
}
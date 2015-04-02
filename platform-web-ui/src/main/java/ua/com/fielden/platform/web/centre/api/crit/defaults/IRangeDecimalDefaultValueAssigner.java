package ua.com.fielden.platform.web.centre.api.crit.defaults;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;

/**
 * A contract to provide a default value assigner for a range kind selection criteria of a big decimal type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeDecimalDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<BigDecimal, T>> assigner);
    IAlsoCrit<T> setFromDefaultValue(final BigDecimal value);
    IAlsoCrit<T> setToDefaultValue(final BigDecimal value);
    IAlsoCrit<T> setFromAndToDefaultValues(final BigDecimal fromValue, final BigDecimal toValue);
}
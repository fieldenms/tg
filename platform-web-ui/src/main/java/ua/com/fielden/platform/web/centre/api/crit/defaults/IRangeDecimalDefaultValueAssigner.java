package ua.com.fielden.platform.web.centre.api.crit.defaults;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;

/**
 * A contract to provide a default value assigner for a range kind selection criteria of a big decimal type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeDecimalDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>> assigner);
    IAlsoCrit<T> setDefaultValue(final RangeCritOtherValueMnemonic<BigDecimal> value);
}
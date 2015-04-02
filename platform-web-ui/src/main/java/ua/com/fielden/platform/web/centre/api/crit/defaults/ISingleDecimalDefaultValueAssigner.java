package ua.com.fielden.platform.web.centre.api.crit.defaults;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.ISingleValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;

/**
 * A contract to provide a default value assigner or specific default values for a single-valued kind selection criteria of any decimal type,
 * which at the moment includes {@link BigDecimal} and {@link Money} types (money should be treated as big decimal).
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleDecimalDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {

    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>> assigner);

    IAlsoCrit<T> setDefaultValue(final SingleCritOtherValueMnemonic<BigDecimal> value);
}
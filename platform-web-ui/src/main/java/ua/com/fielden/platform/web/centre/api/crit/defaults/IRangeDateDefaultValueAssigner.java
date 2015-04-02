package ua.com.fielden.platform.web.centre.api.crit.defaults;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;

/**
 * A contract to provide a default value assigner for a range kind selection criteria of a date/time type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeDateDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<RangeCritDateValueMnemonic, T>> assigner);
    IAlsoCrit<T> setDefaultValue(final RangeCritDateValueMnemonic value);
}
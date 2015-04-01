package ua.com.fielden.platform.web.centre.api.crit.default_assigner;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.default_value.ISingleValueAssigner;

/**
 * A contract to provide a default value assigner or specific default values for a single-valued kind selection criteria of the {@link Date} type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleDateDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {

    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends ISingleValueAssigner<Date, T>> assigner);

    /**
     * Should not accept a <code>null</code> value.
     *
     * @param value
     * @return
     */
    IAlsoCrit<T> setDefaultValue(final Date value);
}
package ua.com.fielden.platform.web.centre.api.crit.defaults;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;

/**
 * A contract to provide a default value assigner for a range kind selection criteria of a date/time type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeDateDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<Date, T>> assigner);
    IAlsoCrit<T> setFromDefaultValue(final Date value);
    IAlsoCrit<T> setToDefaultValue(final Date value);
    IAlsoCrit<T> setFromAndToDefaultValues(final Date fromValue, final Date toValue);

}
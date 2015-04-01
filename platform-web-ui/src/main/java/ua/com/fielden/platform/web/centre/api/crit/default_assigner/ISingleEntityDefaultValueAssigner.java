package ua.com.fielden.platform.web.centre.api.crit.default_assigner;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.default_value.ISingleValueAssigner;

/**
 * A contract to provide a default value assigner or specific default values for a single-valued kind selection criteria of an entity type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleEntityDefaultValueAssigner<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends ISingleValueAssigner<V, T>> assigner);
    IAlsoCrit<T> setDefaultValue(final V value);
}
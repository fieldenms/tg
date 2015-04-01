package ua.com.fielden.platform.web.centre.api.crit.default_assigner;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.default_value.IMultiValueAssigner;

/**
 * A contract to provide a default value assigner or specific default values for a multi-valued kind selection criteria of either string type or an entity type.
 * Multi-valued selection criteria accepts wild-card strings separated by comma.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultiStringDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IMultiValueAssigner<String, T>> assigner);
    IAlsoCrit<T> setDefaultValues(final String... values);
}
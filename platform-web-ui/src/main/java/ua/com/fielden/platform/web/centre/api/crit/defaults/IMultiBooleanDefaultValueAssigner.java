package ua.com.fielden.platform.web.centre.api.crit.defaults;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IMultiValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;

/**
 * A contract to provide a default value assigner or specific default values for a multi-valued kind selection criteria of either string type or an entity type.
 * Multi-valued selection criteria accepts wild-card strings separated by comma.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultiBooleanDefaultValueAssigner<T extends AbstractEntity<?>> extends IAlsoCrit<T> {
    IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IMultiValueAssigner<MultiCritBooleanValueMnemonic, T>> assigner);
    IAlsoCrit<T> setDefaultValues(final MultiCritBooleanValueMnemonic value);
}
package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;

/**
 * A contract for selecting multi-valued criteria.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultiValueCritSelector<T extends AbstractEntity<?>> {

    /**
     * Specifies the need to add a multi-valued autocompleter as a selection criterion, which supports wile card values.
     *
     * @return
     */
    <V extends AbstractEntity<?>> IMultiValueAutocompleterBuilder<T, V> autocompleter(final Class<V> type);

    /**
     * Specified the need to add a multi-valued text-based selection criterion, which support wild card values.
     * It is applicable for many different property types, including phone, colour (expressed in hex codes), email and of course short or long strings.
     *
     * @return
     */
    IMultiStringDefaultValueAssigner<T> text();

    /**
     * Specifies the need to add a multi-valued boolean selection criterion.
     * For boolean criterion this is the most ordinary case, supporting alternation between <code>false/true</code>, neither or both.
     *
     * @return
     */
    IMultiBooleanDefaultValueAssigner<T> bool();
}

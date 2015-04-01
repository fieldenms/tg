package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;

/**
 * A contract for selecting single-valued selection criteria.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISingleValueCritSelector<T extends AbstractEntity<?>> {
    /**
     * Specifies the need to add a single-valued autocompleter as a selection criterion that corresponds to a {@link CritOnly}
     * definition with parameter {@link CritOnly.Type#SINGLE}.
     *
     * @return
     */
    ISingleValueAutocompleterBuilder<T> autocompleter();

    /**
     * Specifies the need to add a single-valued text-based selection criterion, which support wild card values.
     * It is applicable for many different property types, including phone, colour (expressed in hex codes), email and of course short or long strings.
     *
     * @return
     */
    IAlsoCrit<T> text();

    /**
     * Specifies the need to add a single-valued boolean selection criterion.
     * @return
     */
    IAlsoCrit<T> bool();

    /**
     * Specifies the need to add a single-valued integer selection criterion.
     * @return
     */
    IAlsoCrit<T> integer();

    /**
     * Specifies the need to add a single-valued decimal or monetary selection criterion.
     * @return
     */
    IAlsoCrit<T> decimal();

    /**
     * Specifies the need to add a single-valued date selection criterion.
     * @return
     */
    IAlsoCrit<T> date();

    /**
     * Specifies the need to add a single-valued date/time selection criterion.
     * @return
     */
    IAlsoCrit<T> dateTime();

    /**
     * Specifies the need to add a single-valued time selection criterion.
     * @return
     */
    IAlsoCrit<T> time();
}

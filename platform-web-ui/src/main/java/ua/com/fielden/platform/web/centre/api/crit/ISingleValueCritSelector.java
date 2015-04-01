package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.ISingleBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.ISingleDateDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.ISingleDecimalDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.ISingleIntegerDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.ISingleStringDefaultValueAssigner;

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
     * <p>
     * Requires to specify an actual entity type of the property/criterion used, which helps subsequent method chaining in terms of complile type checking.
     *
     * @return
     */
    <V extends AbstractEntity<?>> ISingleValueAutocompleterBuilder<T, V> autocompleter(Class<V> propertyType);

    /**
     * Specifies the need to add a single-valued text-based selection criterion, which support wild card values.
     * It is applicable for many different property types, including phone, colour (expressed in hex codes), email and of course short or long strings.
     *
     * @return
     */
    ISingleStringDefaultValueAssigner<T> text();

    /**
     * Specifies the need to add a single-valued boolean selection criterion.
     * @return
     */
    ISingleBooleanDefaultValueAssigner<T> bool();

    /**
     * Specifies the need to add a single-valued integer selection criterion.
     * @return
     */
    ISingleIntegerDefaultValueAssigner<T> integer();

    /**
     * Specifies the need to add a single-valued decimal or monetary selection criterion.
     * @return
     */
    ISingleDecimalDefaultValueAssigner<T> decimal();

    /**
     * Specifies the need to add a single-valued date selection criterion.
     * @return
     */
    ISingleDateDefaultValueAssigner<T> date();

    /**
     * Specifies the need to add a single-valued date/time selection criterion.
     * @return
     */
    ISingleDateDefaultValueAssigner<T> dateTime();

    /**
     * Specifies the need to add a single-valued time selection criterion.
     * @return
     */
    ISingleDateDefaultValueAssigner<T> time();
}

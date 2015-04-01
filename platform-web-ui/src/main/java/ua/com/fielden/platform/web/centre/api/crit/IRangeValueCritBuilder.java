package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.IRangeDateDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.IRangeDecimalDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.default_assigner.IRangeIntegerDefaultValueAssigner;

/**
 * A contract for specifying selection criteria as ranges.
 * Selection criteria as ranges are applicable to numeric properties, including monetary type, and date/time.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRangeValueCritBuilder<T extends AbstractEntity<?>> {
    /**
     * Specifies the need to add a range selection criterion as integer.
     *
     * @return
     */
    IRangeIntegerDefaultValueAssigner<T> integer();

    /**
     * Specifies the need to add a range selection criterion as decimal, which includes monetary type.
     *
     * @return
     */
    IRangeDecimalDefaultValueAssigner<T> decimal();

    /**
     * Specifies the need to add a range selection criterion as date without a time portion.
     *
     * @return
     */
    IRangeDateDefaultValueAssigner<T> date();

    /**
     * Specifies the need to add a range selection criterion as date with time portion (this is the most common case).
     *
     * @return
     */
    IRangeDateDefaultValueAssigner<T> dateTime();

    /**
     * Specifies the need to add a range selection criterion as time (no date portion).
     *
     * @return
     */
    IRangeDateDefaultValueAssigner<T> time();
}

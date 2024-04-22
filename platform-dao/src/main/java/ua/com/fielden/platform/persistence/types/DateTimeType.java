package ua.com.fielden.platform.persistence.types;

import java.util.Date;
import java.util.TimeZone;

/**
 * Custom Hibernate type mapping for {@link Date} in the system's default time zone.
 *
 * @author TG Team
 */
public final class DateTimeType extends OffsetDateTimeType {

    public static final DateTimeType INSTANCE = new DateTimeType();

    public DateTimeType() {
        super(TimeZone::getDefault);
    }

}

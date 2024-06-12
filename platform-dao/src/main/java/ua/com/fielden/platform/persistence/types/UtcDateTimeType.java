package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;
import java.util.TimeZone;

import static java.util.Objects.requireNonNull;

/**
 * This is a user type that should be used to store properties of type {@link Date} in UTC.
 * For this to take effect, a corresponding property needs to be annotated with <code>@PersistentType(userType = IUtcDateTimeType.class)</code>
 *  
 * @author TG Team
 *
 */
public final class UtcDateTimeType extends OffsetDateTimeType implements IUtcDateTimeType {

    private static final TimeZone UTC = requireNonNull(TimeZone.getTimeZone("UTC"), "Couldn't obtain UTC time zone.");

    public static final UtcDateTimeType INSTANCE = new UtcDateTimeType();

    public UtcDateTimeType() {
        super(() -> UTC);
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        return argument;
    }

}

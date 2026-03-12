package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;
import java.util.TimeZone;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.persistence.types.exceptions.UserTypeException.invalidPersistedRepresentation;

public final class UtcDateTimeType extends OffsetDateTimeType implements IUtcDateTimeType {

    private static final TimeZone UTC = requireNonNull(TimeZone.getTimeZone("UTC"), "Could not obtain time zone UTC.");

    public static final UtcDateTimeType INSTANCE = new UtcDateTimeType();

    public UtcDateTimeType() {
        super(() -> UTC);
    }

    @Override
    public Date instantiate(final Object argument, final EntityFactory factory) {
        return switch (argument) {
            case Date date -> date;
            case null -> null;
            default -> throw invalidPersistedRepresentation("Date", argument);
        };
    }

}

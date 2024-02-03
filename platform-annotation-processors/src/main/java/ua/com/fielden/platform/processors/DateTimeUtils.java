package ua.com.fielden.platform.processors;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    /**
     * Returns ISO-8601 formatted representation of the given temporal object.
     */
    public static String toIsoFormat(Temporal temporal) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(temporal);
    }

    public static ZonedDateTime zonedNow() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

}

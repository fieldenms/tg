package ua.com.fielden.platform.parser;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.time.Duration;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.parser.IValueParser.Result.ok;

/**
 * Parses a duration according to the following format: a non-negative decimal followed by a {@linkplain DurationParser#UNITS unit character}.
 */
public final class DurationParser implements IValueParser<Object, Duration> {

    public static final String ERROR_INVALID_DURATION = "Invalid duration: '%s'. Expected a decimal value followed by a unit (%s).";

    @Override
    public Result<Duration> apply(final Object value) {
        final var str = requireNonNull(value).toString();
        final int unitIdx = StringUtils.indexOfAny(str, UNITS);
        if (unitIdx <= 0) {
            return incorrectFormat(str);
        }

        final long number;
        try {
            number = Long.parseLong(str.substring(0, unitIdx));
        } catch (final NumberFormatException e) {
            return incorrectFormat(str, e);
        }

        final Duration duration;
        try {
            final char unit = str.charAt(unitIdx);
            duration = switch (unit) {
                case 's' -> Duration.ofSeconds(number);
                case 'm' -> Duration.ofMinutes(number);
                case 'h' -> Duration.ofHours(number);
                case 'd' -> Duration.ofDays(number);
                default -> throw new InvalidArgumentException("Invalid unit: %s".formatted(unit));
            };
        } catch (final RuntimeException e) {
            return incorrectFormat(str, e);
        }

        return ok(duration);
    }

    private static final char[] UNITS = {
            's', // seconds
            'm', // minutes
            'h', // hours
            'd', // days
    };

    private static <T> Result<T> incorrectFormat(final String str) {
        return Result.error(ERROR_INVALID_DURATION.formatted(str, Arrays.toString(UNITS)));
    }

    private static <T> Result<T> incorrectFormat(final String str, final Throwable cause) {
        return Result.error(ERROR_INVALID_DURATION.formatted(str, Arrays.toString(UNITS)), cause);
    }

}

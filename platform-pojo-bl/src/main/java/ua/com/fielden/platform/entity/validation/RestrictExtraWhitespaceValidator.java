package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.tuples.T3.t3;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * A validator that restricts extra whitespace in a {@code String}-typed property.
 * Extra whitespace includes: leading, trailing or consecutive (2 or more whitespace characters between non-whitespace).
 *
 * @author TG Team
 */
public class RestrictExtraWhitespaceValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_LEADING_WHITESPACE = "Leading whitespace characters are not permitted.";
    public static final String ERR_CONTAINS_LEADING_WHITESPACE_VALUE = "Leading whitespace characters are not permitted: [%s]";

    public static final String ERR_CONTAINS_TRAILING_WHITESPACE = "Trailing whitespace characters are not permitted.";
    public static final String ERR_CONTAINS_TRAILING_WHITESPACE_VALUE = "Trailing whitespace characters are not permitted: [%s]";

    public static final String ERR_CONTAINS_CONSECUTIVE_WHITESPACE = "Consecutive whitespace characters are not permitted.";
    public static final String ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE = "Consecutive whitespace characters are not permitted: [%s]";

    private static final Pattern PATTERN_LEADING_WHITESPACE = Pattern.compile("^\\s+");
    private static final Pattern PATTERN_TRAILING_WHITESPACE = Pattern.compile("\\s+$");
    // this pattern does not strictly match between non-whitespace characters, but if checked last, it will do the trick
    private static final Pattern PATTERN_CONSECUTIVE_WHITESPACE = Pattern.compile("\\s{2,}");

    private static final String WHITESPACE_REPLACEMENT = "{?}";
    public static final int MAX_REPORTABLE_LENGTH = 64;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful(newValue);
        }

        return Stream.of(
                t3(PATTERN_LEADING_WHITESPACE, ERR_CONTAINS_LEADING_WHITESPACE, ERR_CONTAINS_LEADING_WHITESPACE_VALUE),
                t3(PATTERN_TRAILING_WHITESPACE, ERR_CONTAINS_TRAILING_WHITESPACE, ERR_CONTAINS_TRAILING_WHITESPACE_VALUE),
                t3(PATTERN_CONSECUTIVE_WHITESPACE, ERR_CONTAINS_CONSECUTIVE_WHITESPACE, ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE))
                .map(p -> check(newValue, p._1, p._2, p._3))
                // find first match and return its result, otherwise return success
                .filter(res -> !res.isSuccessfulWithoutWarningAndInformative())
                .findFirst().orElse(successful(newValue));
    }

    private static Result check(final String value, final Pattern pattern, final String message, final String messageWithValue) {
        final Matcher matcher = pattern.matcher(value);

        if (!matcher.find()) {
            return successful(value);
        }

        if (value.length() >= MAX_REPORTABLE_LENGTH) {
            return failure(message);
        }

        final String reportValue = matcher.replaceAll(WHITESPACE_REPLACEMENT);
        return failure(format(messageWithValue, reportValue));
    }

}
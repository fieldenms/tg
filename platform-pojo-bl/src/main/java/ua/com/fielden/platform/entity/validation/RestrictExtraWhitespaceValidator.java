package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.types.tuples.T2.t2;

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
 * @author homedirectory
 */
public class RestrictExtraWhitespaceValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_LEADING_WHITESPACE = "Value contains leading whitespace.";
    public static final String ERR_CONTAINS_LEADING_WHITESPACE_VALUE = "Value contains leading whitespace: [%s]";

    public static final String ERR_CONTAINS_TRAILING_WHITESPACE = "Value contains trailing whitespace.";
    public static final String ERR_CONTAINS_TRAILING_WHITESPACE_VALUE = "Value contains trailing whitespace: [%s]";

    public static final String ERR_CONTAINS_CONSECUTIVE_WHITESPACE = "Value contains consecutive whitespace.";
    public static final String ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE = "Value contains consecutive whitespace: [%s]";

    private static final Pattern PATTERN_LEADING_WHITESPACE = Pattern.compile("^\\s+");
    private static final Pattern PATTERN_TRAILING_WHITESPACE = Pattern.compile("\\s+$");
    // this pattern does not strictly match between non-whitespace characters, but if checked last, it will do the trick
    private static final Pattern PATTERN_CONSECUTIVE_WHITESPACE = Pattern.compile("\\s{2,}");

    private static final String WHITESPACE_REPLACEMENT = "{?}";
    public static final int MAX_REPORTABLE_LENGTH = 64;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return Result.successful(newValue);
        }

        return Stream.of(
                t2(PATTERN_LEADING_WHITESPACE, t2(ERR_CONTAINS_LEADING_WHITESPACE, ERR_CONTAINS_LEADING_WHITESPACE_VALUE)),
                t2(PATTERN_TRAILING_WHITESPACE, t2(ERR_CONTAINS_TRAILING_WHITESPACE, ERR_CONTAINS_TRAILING_WHITESPACE_VALUE)),
                t2(PATTERN_CONSECUTIVE_WHITESPACE, t2(ERR_CONTAINS_CONSECUTIVE_WHITESPACE, ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE)))
                .map(p -> check(newValue, p._1, p._2._1, p._2._2))
                // find first match and return its result, otherwise return success
                .filter(res -> !res.isSuccessfulWithoutWarningAndInformative())
                .findFirst().orElse(Result.successful(newValue));
    }

    private static Result check(final String value, final Pattern pattern, final String message, final String messageWithValue) {
        final Matcher matcher = pattern.matcher(value);

        if (!matcher.find()) {
            return Result.successful(value);
        }

        if (value.length() >= MAX_REPORTABLE_LENGTH) {
            return Result.failure(message);
        }

        final String reportValue = matcher.replaceAll(WHITESPACE_REPLACEMENT);
        return Result.failure(format(messageWithValue, reportValue));
    }

}
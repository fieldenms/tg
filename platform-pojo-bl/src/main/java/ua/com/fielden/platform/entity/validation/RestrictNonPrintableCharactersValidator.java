package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.base.CharMatcher;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * A validator that restricts invisible/non-printable characters, except a space, in a {@code String}-typed property.
 * <p>
 * The definition of invisible/non-printable characters is as per Google Guava's {@link CharMatcher#invisible()}.
 * Strictly speaking this means that it would only work for BMP characters, but not for supplementary characters (e.g., emoji would get recognised as non-printable, which is suitable for the purpose of restricting key values).
 *
 * @author TG Team
 */
public class RestrictNonPrintableCharactersValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_NON_PRINTABLE = "Non-printable characters are not permitted.";
    public static final String ERR_CONTAINS_NON_PRINTABLE_VALUE = "Non-printable characters are not permitted: [%s]";

    private static final String NON_PRINTABLE_REPLACEMENT = "{?}";
    public static final int MAX_REPORTABLE_LENGTH = 64;

    private static final CharMatcher MATCHER = CharMatcher.invisible().and(CharMatcher.isNot(' ')).precomputed();
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful(newValue);
        }

        if (MATCHER.matchesNoneOf(newValue)) {
            return successful(newValue);
        }

        if (newValue.length() >= MAX_REPORTABLE_LENGTH) {
            return failure(ERR_CONTAINS_NON_PRINTABLE);
        }

        final String reportValue = MATCHER.replaceFrom(newValue, NON_PRINTABLE_REPLACEMENT);
        return failure(format(ERR_CONTAINS_NON_PRINTABLE_VALUE, reportValue));
    }

}
package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * A validator that restricts non-printable characters in a {@code String}-typed property.
 * <p>
 * Printable characters include:
 * <ul>
 *   <li>alphanumeric characters</li>
 *   <li>punctuation: one of <code>!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~</code> </li>
 *   <li>a single space</li>
 * </ul>
 *
 * @author TG Team
 */
public class RestrictNonPrintableCharactersValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_NON_PRINTABLE = "Non-printable characters are not permitted.";
    public static final String ERR_CONTAINS_NON_PRINTABLE_VALUE = "Non-printable characters are not permitted: [%s]";

    private static final Pattern PATTERN_NON_PRINTABLE = Pattern.compile("[^\\p{Print}]");
    private static final String NON_PRINTABLE_REPLACEMENT = "{?}";
    public static final int MAX_REPORTABLE_LENGTH = 64;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful(newValue);
        }

        final Matcher matcher = PATTERN_NON_PRINTABLE.matcher(newValue);

        if (!matcher.find()) {
            return successful(newValue);
        }

        if (newValue.length() >= MAX_REPORTABLE_LENGTH) {
            return failure(ERR_CONTAINS_NON_PRINTABLE);
        }

        final String reportValue = matcher.replaceAll(NON_PRINTABLE_REPLACEMENT);
        return failure(format(ERR_CONTAINS_NON_PRINTABLE_VALUE, reportValue));
    }

}
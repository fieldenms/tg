package ua.com.fielden.platform.entity.validation;

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
 * @author homedirectory
 */
public class RestrictNonPrintableCharactersValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_NON_PRINTABLE = "Value contains non-printable characters.";
    public static final String ERR_CONTAINS_NON_PRINTABLE_VALUE = "Value contains non-printable characters: [%s]";

    private static final Pattern PATTERN_NON_PRINTABLE = Pattern.compile("[^\\p{Print}]");
    private static final String NON_PRINTABLE_REPLACEMENT = "{?}";
    public static final int MAX_REPORTABLE_LENGTH = 64;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return Result.successful(newValue);
        }

        final Matcher matcher = PATTERN_NON_PRINTABLE.matcher(newValue);

        if (!matcher.find()) {
            return Result.successful(newValue);
        }

        if (newValue.length() >= MAX_REPORTABLE_LENGTH) {
            return Result.failure(ERR_CONTAINS_NON_PRINTABLE);
        }

        final String reportValue = matcher.replaceAll(NON_PRINTABLE_REPLACEMENT);
        return Result.failure(String.format(ERR_CONTAINS_NON_PRINTABLE_VALUE, reportValue));
    }

}
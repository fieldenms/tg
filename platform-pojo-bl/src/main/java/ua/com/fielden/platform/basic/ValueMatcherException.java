package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * An exception pertaining to value matching.
 *
 * @see IValueMatcher
 */
public class ValueMatcherException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERR_UNSUPPORTED_MATCH_AGAINST = "[%s] does not support matching against [%s].";

    public ValueMatcherException(final String s) {
        super(s);
    }

    public ValueMatcherException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static ValueMatcherException unsupportedValueType(final Class<? extends IValueMatcher> matcherType, final Class<?> valueType) {
        return new ValueMatcherException(ERR_UNSUPPORTED_MATCH_AGAINST.formatted(matcherType.getTypeName(), valueType.getTypeName()));
    }

}

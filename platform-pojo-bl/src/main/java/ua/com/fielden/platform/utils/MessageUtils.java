package ua.com.fielden.platform.utils;

import static java.lang.String.format;

import java.math.BigDecimal;

/**
 * Utilities for handling plural/singular situations when composing messages.
 *
 * @author TG Team
 *
 */
public class MessageUtils {
    
    private MessageUtils() {}
    
    /** Returns something like "is 1 apple" or "are 2 oranges" (but remember that you cannot compare apples to oranges)
     */
    public static String singleOrPlural(final String isStr, final String areStr, final long kount, final String single, final String plural) {
        final String result;

        if (Math.abs(kount) == 1) {
            result = format("%s %d %s", isStr, kount, single);
        } else {
            result = format("%s %d %s", areStr, kount, plural);
        }
        return result.trim();
    }

    public static String singleOrPlural(final String isStr, final String areStr, final BigDecimal kount, final String single, final String plural) {
        final String result;

        if ((BigDecimal.ONE.compareTo(kount) == 0) || (BigDecimal.ONE.compareTo(kount.negate()) == 0)) {
            result = format("%s %s %s", isStr, kount.stripTrailingZeros().toPlainString(), single);
        } else {
            result = format("%s %s %s", areStr, kount.stripTrailingZeros().toPlainString(), plural);
        }

        return result.trim();
    }

    public static String singleOrPlural(final BigDecimal amount, final String single, final String plural) {
        if ((BigDecimal.ONE.compareTo(amount) == 0) || (BigDecimal.ONE.compareTo(amount.negate()) == 0)) {
            return single;
        } else {
            return plural;
        }
    }
    
    public static String singleOrPlural(final Integer number, final String single, final String plural) {
        return singleOrPlural(BigDecimal.valueOf(number), single, plural);
    }


}

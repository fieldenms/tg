package ua.com.fielden.platform.utils;

import java.util.EnumSet;

/**
 * Provides named unicode constants.
 * <p>
 * Please refer {@linkplain http://www.alanwood.net/unicode/arrows.html} for more details.
 * 
 * @author TG Team
 * 
 */
public enum UnicodeConsts {

    ARROW_LEFTWARDS('←'), // \u2190
    ARROW_UPWARDS('↑'), // \u2191
    ARROW_RIGHTWARDS('→'), // \u2192
    ARROW_DOWNWARDS('↓'), // \u2193
    ARROW_LEFTWARDS_WHITE('⇦'), // \u21E6
    ARROW_UPWARDS_WHITE('⇧'), // \u21E7
    ARROW_RIGHTWARDS_WHITE('⇨'), // \u21E8
    ARROW_DOWNWARDS_WHITE('⇩'); // \u21E7

    public final char unicode;

    UnicodeConsts(final char unicode) {
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return String.valueOf(unicode);
    }

    public static void main(final String[] args) {
        for (final UnicodeConsts con : EnumSet.allOf(UnicodeConsts.class)) {
            System.out.println(con.name() + ":\t" + con);
        }
    }
}

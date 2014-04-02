package ua.com.fielden.platform.swing.menu.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFilter} implementation, which supports wild cards and multiple criteria (comma separated).
 * 
 * @author 01es
 * 
 */
public class StringFilter implements IFilter {
    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean filter(final Object value, final String valuefilterCrit) {
        boolean result = false;
        final String[] criteria = valuefilterCrit.split(",");
        for (final String crit : criteria) {
            final String fullPattern = crit.trim().toUpperCase();
            final String fullPatternS1 = fullPattern.startsWith("\\*") ? removeFromStart(fullPattern) : "^" + fullPattern;
            final String fullPatternS2 = fullPatternS1.endsWith("\\*") ? removeFromEnd(fullPatternS1) : fullPatternS1;
            final String fullPatternS3 = fullPatternS2.replaceAll("\\*", ".*");
            final String strPattern = fullPatternS3.indexOf("*") > -1 ? fullPatternS3 + "$" : fullPatternS3 + ".*";

            final String fullName = value.toString().toUpperCase();
            final Pattern pattern = Pattern.compile(strPattern);
            final Matcher matcher = pattern.matcher(fullName);

            result = result || matcher.find();
        }
        return !result;
    }

    /**
     * A convenient method to remove leading wild cards.
     * 
     * @param value
     * @return
     */
    private String removeFromStart(final String value) {
        final String result = value.startsWith("\\*") ? value.substring(1) : value;
        return value.startsWith("\\*") ? removeFromStart(result) : result;
    }

    /**
     * A convenient method to remove trailing wild cards.
     * 
     * @param value
     * @param whatToRemove
     * @return
     */
    private String removeFromEnd(final String value) {
        final String result = value.endsWith("\\*") ? value.substring(0, value.length() - 1) : value;
        return value.endsWith("\\*") ? removeFromEnd(result) : result;
    }

    public static void main(final String[] args) {
        final StringFilter flt = new StringFilter();

        final String value = "scala rocks";
        System.out.println(flt.filter(value, "*"));
        System.out.println(flt.filter(value, "scala rocks"));
        System.out.println(flt.filter(value, "something else"));
        System.out.println(flt.filter(value, "scala *"));
        System.out.println(flt.filter(value, "*rocks"));
        System.out.println(flt.filter(value, "*sca*ro*"));
        System.out.println(flt.filter(value, "sca*ro*"));
        System.out.println(flt.filter(value, "*ca*rocks"));
        System.out.println(flt.filter(value, ""));

    }

}

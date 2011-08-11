package ua.com.fielden.platform.swing.menu.filter;

import org.apache.commons.lang.StringUtils;

/**
 * {@link IFilter} implementation supporting multiple values (comma separated) where each values is tested for "contains" against a tested object.
 * 
 * @author 01es
 * 
 */
public class WordFilter implements IFilter {
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
	int emptyCount = 0;
	final String[] criteria = valuefilterCrit.split(",");
	for (final String crit : criteria) {
	    final String word = crit.trim().toUpperCase();
	    if (!StringUtils.isEmpty(word)) {
		result = result || value.toString().toUpperCase().contains(word);
	    } else {
		emptyCount++;
	    }
	}
	return emptyCount == criteria.length ? false : !result;
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
	final WordFilter flt = new WordFilter();

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

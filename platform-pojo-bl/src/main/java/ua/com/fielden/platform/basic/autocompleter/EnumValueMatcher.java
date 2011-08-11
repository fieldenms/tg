package ua.com.fielden.platform.basic.autocompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.equery.fetch;

/**
 * Provides a enumeration driven implementation of the {@link IValueMatcher} with wild card support. This implementation should be convenient in cases where there is a property of
 * some enumeration type that needs to be populated via an autocompleter.
 *
 * @author TG Team
 *
 */
public class EnumValueMatcher<T extends Enum<T>> implements IValueMatcher<T> {
    private EnumSet<T> values;

    public EnumValueMatcher(final Class<T> enumType) {
	values = EnumSet.allOf(enumType);
    }

    /**
     * Set the values to search items those match some pattern.
     *
     * @param values
     */
    public void setValuesToSearchFor(final Collection<T> values) {
	if (values.isEmpty()) {
	    this.values.clear();
	} else {
	    this.values = EnumSet.copyOf(values);
	}
    }

    /**
     * Returns the size of all values set.
     *
     * @return
     */
    public int getValuesSize() {
	return values.size();
    }

    @Override
    public List<T> findMatches(final String v) {
	final String value = v.toUpperCase();
	final List<T> possibleEntities = new ArrayList<T>();
	final int substringLen = value.length();
	if (substringLen == 0) {
	    return possibleEntities;
	}

	// * if string does not start with % then prepend ^
	// * if string does not end with % then append $
	// * substitute all occurrences of % with .*
	final String prefex = value.startsWith("%") ? "" : "^";
	final String postfix = value.endsWith("%") ? "" : "$";
	final String strPattern = prefex + value.replaceAll("\\%", ".*") + postfix;

	final Pattern pattern = Pattern.compile(strPattern);
	for (final T el : values) {
	    final Matcher matcher = pattern.matcher(el.name());
	    if (matcher.find()) {
		possibleEntities.add(el);
	    }

	}
	return possibleEntities;

    }

    /**
     * Returns true if value matches valuePattern, false otherwise. This method behaves like autocompleter's value matcher
     *
     * @param value
     * @param valuePattern
     * @return
     */
    public static boolean valueMatchesPattern(final String value, String valuePattern) {
	valuePattern = valuePattern.contains("*") ? valuePattern.replaceAll("\\*", "%") : valuePattern + "%";

	final String prefex = valuePattern.startsWith("%") ? "" : "^";
	final String postfix = valuePattern.endsWith("%") ? "" : "$";
	final String strPattern = prefex + valuePattern.replaceAll("\\%", ".*") + postfix;

	return Pattern.compile(strPattern).matcher(value).find();
    }

    /**
     * Converts auto-completer-like regular expression to normal regular expression (simply replaces all '*' with '%' characters)
     *
     * @param autocompleterExp
     * @return
     */
    public static String prepare(final String autocompleterExp) {
	if ("*".equals(autocompleterExp.trim())) {
	    return null;
	}
	return autocompleterExp.replaceAll("\\*", "%").trim();
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
	return findMatches(value);
    }

    @Override
    public void setFetchModel(final fetch fetchModel) {
	throw new UnsupportedOperationException("Entity query model is not supported by POJO value matcher.");
    }

    @Override
    public fetch<?> getFetchModel() {
	throw new UnsupportedOperationException("Entity query model is not supported by POJO value matcher.");
    }

    @Override
    public Integer getPageSize() {
	return null;
    }

}

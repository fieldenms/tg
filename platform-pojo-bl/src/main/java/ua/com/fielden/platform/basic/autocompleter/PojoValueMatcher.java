package ua.com.fielden.platform.basic.autocompleter;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a collection-based implementation of the {@link IValueMatcher} with wild card support.
 * This implementation should be convenient in cases where there is a list of entity instances, which is used to value autocompletion.
 *
 * @param <T>  a type of entities being matched.
 */
public class PojoValueMatcher<T extends AbstractEntity<?>> implements IValueMatcher<T> {

    public static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    private final Collection<T> instances;
    private final boolean isCaseSensitive;
    private final String propName;

    /**
     * Controls the number of values that can be returned as the result of matching.
     */
    private final int limit;

    public PojoValueMatcher(final Collection<T> instances, final String propNameToMatchBy, final int limit) {
        this(instances, propNameToMatchBy, limit, false);
    }

    public PojoValueMatcher(final Collection<T> instances, final String propNameToMatchBy, final int limit, final boolean isCaseSensitive) {
        this.instances = ImmutableList.copyOf(instances);
        this.propName = propNameToMatchBy;
        this.limit = limit;
        this.isCaseSensitive = isCaseSensitive;
    }

    public Collection<T> getInstances() {
        return instances;
    }

    @Override
    public List<T> findMatches(final String v) {
        final String searchText = SPECIAL_REGEX_CHARS.matcher(isCaseSensitive ? v : v.toUpperCase()).replaceAll("\\\\$0");
        final var possibleEntities = new ArrayList<T>();
        final int substringLen = searchText.length();
        if (substringLen == 0) {
            return possibleEntities;
        }

        // * if string does not start with % then prepend ^
        // * if string does not end with % then append $
        // * substitute all occurrences of % with .*
        final String prefix = searchText.startsWith("%") ? "" : "^";
        final String postfix = searchText.endsWith("%") ? "" : "$";
        final String searchPattern = prefix + searchText.replaceAll("%", ".*") + postfix;

        final Pattern pattern = Pattern.compile(searchPattern);
        for (final T instance : instances) {
            if (possibleEntities.size() < limit) {
                final var propValue = instance.get(propName);
                if (propValue != null) {
                    final String value = isCaseSensitive ? propValue.toString() : propValue.toString().toUpperCase();
                    final Matcher matcher = pattern.matcher(value);
                    if (matcher.find()) {
                        possibleEntities.add(instance);
                    }
                }
            }
        }
        return possibleEntities;

    }

    /**
     * Returns the maximum number of values that could be returned by the matcher instance.
     * For example, if there are 100 matching values, but the limit is 10 than only 10 values would be returned from method findMatches().
     */
    @Override
    public Integer getPageSize() {
        return limit;
    }

}
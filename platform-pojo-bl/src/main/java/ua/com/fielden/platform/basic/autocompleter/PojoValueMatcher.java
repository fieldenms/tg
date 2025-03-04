package ua.com.fielden.platform.basic.autocompleter;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.RichText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
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
    private final BiPredicate<Pattern, T> matchingPredicate;

    /**
     * Controls the number of values that can be returned as the result of matching.
     */
    private final int limit;

    public PojoValueMatcher(final Collection<T> instances, final String propNameToMatchBy, final int limit) {
        this(instances, matchByPropPredicate(propNameToMatchBy), limit);
    }

    public PojoValueMatcher(final Collection<T> instances, final BiPredicate<Pattern, T> matchingPredicate, final int limit) {
        this.instances = ImmutableList.copyOf(instances);
        this.limit = limit;
        this.matchingPredicate = matchingPredicate;
    }

    public Collection<T> getInstances() {
        return instances;
    }

    @Override
    public List<T> findMatches(final String v) {
        final String searchText = SPECIAL_REGEX_CHARS.matcher(v.toUpperCase()).replaceAll("\\\\$0");
        final var matchingEntities = new ArrayList<T>();
        final int substringLen = searchText.length();
        if (substringLen == 0) {
            return matchingEntities;
        }

        // * if string does not start with % then prepend ^
        // * if string does not end with % then append $
        // * substitute all occurrences of % with .*
        final String prefix = searchText.startsWith("%") ? "" : "^";
        final String postfix = searchText.endsWith("%") ? "" : "$";
        final String searchPattern = prefix + searchText.replaceAll("%", ".*") + postfix;

        final Pattern pattern = Pattern.compile(searchPattern);
        for (final T entity : instances) {
            if (matchingEntities.size() < limit) {
                if (matchingPredicate.test(pattern, entity)) {
                    matchingEntities.add(entity);
                }
            }
        }
        return matchingEntities;

    }

    /**
     * Returns the maximum number of values that could be returned by the matcher instance.
     * For example, if there are 100 matching values, but the limit is 10 than only 10 values would be returned from method findMatches().
     */
    @Override
    public Integer getPageSize() {
        return limit;
    }

    public static <T extends AbstractEntity<?>> BiPredicate<Pattern, T> matchByPropPredicate(final String propNameToMatchBy) {
        return matchByAnyPropPredicate(Set.of(propNameToMatchBy));
    }

    public static <T extends AbstractEntity<?>> BiPredicate<Pattern, T> matchByAnyPropPredicate(final Set<String> propNamesToMatchBy) {
        return (pattern, entity) -> {
            return propNamesToMatchBy.stream()
                    .map(entity::get)
                    .anyMatch(value -> valueMatches(value, pattern));
        };
    }

    private static boolean valueMatches(final Object value, final Pattern pattern) {
        if (value == null) {
            return false;
        }
        if (value instanceof RichText richText) {
            return pattern.matcher(richText.searchText().toUpperCase()).find();
        }
        else {
            return pattern.matcher(value.toString().toUpperCase()).find();
        }
    }

}

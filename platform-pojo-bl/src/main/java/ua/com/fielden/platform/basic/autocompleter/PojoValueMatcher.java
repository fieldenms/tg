package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ExpExec;

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
    private final ExpExec<T> exec = new ExpExec<>("pojo");
    private final boolean isCaseSensitive;

    /**
     * Controls the number of values that can be returned as the result of matching.
     */
    private final int limit;

    public PojoValueMatcher(final Collection<T> instances, final String expression, final int limit) {
        this(instances, expression, limit, false);
    }

    public PojoValueMatcher(final Collection<T> instances, final String expression, final int limit, final boolean isCaseSensitive) {
        this.instances = instances;
        exec.add(expression);
        this.limit = limit;
        this.isCaseSensitive = isCaseSensitive;
    }

    /*
     * Two protected getters to make this class overridable.
     */
    protected Collection<T> getInstances() {
        return instances;
    }

    protected ExpExec<T> getExec() {
        return exec;
    }
    
    @Override
    public List<T> findMatches(final String v) {
        final String value = SPECIAL_REGEX_CHARS.matcher(isCaseSensitive ? v : v.toUpperCase()).replaceAll("\\\\$0");
        final List<T> possibleEntities = new ArrayList<T>();
        final int substringLen = value.length();
        if (substringLen == 0) {
            return possibleEntities;
        }

        // * if string does not start with % then prepend ^
        // * if string does not end with % then append $
        // * substitute all occurrences of % with .*
        final String prefix = value.startsWith("%") ? "" : "^";
        final String postfix = value.endsWith("%") ? "" : "$";
        final String strPattern = prefix + value.replaceAll("%", ".*") + postfix;

        final Pattern pattern = Pattern.compile(strPattern);
        for (final T instance : instances) {
            if (possibleEntities.size() < limit) {
                final Object entryValue = exec.eval(instance, 0);
                if (entryValue != null) {
                    final String listEntry = isCaseSensitive ? entryValue.toString() : entryValue.toString().toUpperCase();
                    final Matcher matcher = pattern.matcher(listEntry);
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
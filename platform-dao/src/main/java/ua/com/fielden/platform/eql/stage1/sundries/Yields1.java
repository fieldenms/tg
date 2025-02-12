package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.utils.ToString;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public record Yields1 (SortedMap<String, Yield1> yieldsMap) implements ToString.IFormattable {
    public static final Yields1 EMPTY_YIELDS = new Yields1(emptyList());

    public static Yields1 yields(final Collection<Yield1> yields) {
        return yields.isEmpty() ? EMPTY_YIELDS : new Yields1(yields);
    }

    public static Yields1 yields(final Yield1... yields) {
        return yields.length == 0 ? EMPTY_YIELDS : new Yields1(yields);
    }

    private Yields1(final Collection<Yield1> yields) {
        this(makeYieldsMap(yields));
    }

    private Yields1(final Yield1... yields) {
        this(Arrays.asList(yields));
    }

    public Yields2 transform(final TransformationContextFromStage1To2 context) {
        return yieldsMap.isEmpty() ? Yields2.EMPTY_YIELDS : new Yields2(yieldsMap.values().stream().map(el -> el.transform(context)).collect(toList()));
    }

    public Collection<Yield1> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }

    public boolean isEmpty() {
        return yieldsMap.isEmpty();
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return yieldsMap.isEmpty() ? emptySet() : yieldsMap.values().stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    private static SortedMap<String, Yield1> makeYieldsMap(final Collection<Yield1> yields) {
        final var map = new TreeMap<String, Yield1>();

        for (final Yield1 yield : yields) {
            if (map.containsKey(yield.alias())) {
                throw new EqlStage1ProcessingException(
                        String.format("Query contains duplicate yields for alias [%s].", yield.alias()));
            }
            map.put(yield.alias(), yield);
        }

        return unmodifiableSortedMap(map);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yieldsMap", yieldsMap)
                .$();
    }

}

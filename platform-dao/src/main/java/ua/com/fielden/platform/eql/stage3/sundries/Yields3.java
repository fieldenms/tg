package ua.com.fielden.platform.eql.stage3.sundries;

import com.google.common.collect.ImmutableSortedMap;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ToString;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableSortedMap;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

public record Yields3 (SortedMap<String, Yield3> yieldsMap) implements ToString.IFormattable {

    public Yields3(final List<Yield3> yields) {
        this(makeYieldsMap(yields));
    }

    private static SortedMap<String, Yield3> makeYieldsMap(final List<Yield3> yields) {
        // We need to support duplicate map keys, hence manual map population.
        if (yields.isEmpty()) {
            return ImmutableSortedMap.of();
        }
        else {
            final var map = new TreeMap<String, Yield3>();
            yields.forEach(y -> map.put(y.alias(), y));
            return unmodifiableSortedMap(map);
        }
    }

    public boolean isEmpty() {
        return yieldsMap.isEmpty();
    }

    public int size() {
        return yieldsMap.size();
    }
    
    public Collection<Yield3> getYields() {
        return yieldsMap.values();
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final List<PropType> expectedTypes) {
        if (expectedTypes.size() != yieldsMap.size()) {
            throw new EqlStage3ProcessingException("""
                    Mismatch between number of yields and their expected types.
                    Yields: %s [%s]
                    Types : %s [%s].""".formatted(
                    yieldsMap.size(), CollectionUtil.toString(yieldsMap.values(), ", "),
                    expectedTypes.size(), CollectionUtil.toString(expectedTypes, ", ")));
        }

        return "SELECT\n" +
                zip(getYields().stream(), expectedTypes.stream(), (y, type) -> y.sql(metadata, dbVersion, type))
                        .collect(joining(", "));
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return "SELECT\n" + getYields().stream().map(y -> y.sql(metadata, dbVersion)).collect(joining(", "));
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yields", yieldsMap)
                .$();
    }

}

package ua.com.fielden.platform.eql.stage3.sundries;

import com.google.common.collect.ImmutableSortedMap;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableSortedMap;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.StreamUtils.zip;
import static ua.com.fielden.platform.utils.ToString.separateLines;

public record Yields3 (SortedMap<String, Yield3> yieldsMap) implements ToString.IFormattable {

    public static final String ERR_YIELDS_MISMATCH = "Mismatch between the number of yields and their expected types: %s yield(s), but %s type(s).";

    private static final Logger LOGGER = getLogger();

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
            LOGGER.error(() -> separateLines().toString(
                                         new EqlStage3ProcessingException(ERR_YIELDS_MISMATCH.formatted(yieldsMap.size(), expectedTypes.size())).getMessage())
                                 .add("expectedTypes", expectedTypes)
                                 .add("yields", yieldsMap)
                                 .$(),
                         new EqlStage3ProcessingException(ERR_YIELDS_MISMATCH.formatted(yieldsMap.size(), expectedTypes.size())));
            throw new EqlStage3ProcessingException(ERR_YIELDS_MISMATCH.formatted(yieldsMap.size(), expectedTypes.size()));
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
        return toString(separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yields", yieldsMap)
                .$();
    }

}

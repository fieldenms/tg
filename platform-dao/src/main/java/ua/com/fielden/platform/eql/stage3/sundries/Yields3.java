package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

public class Yields3 {

    private final SortedMap<String, Yield3> yieldsMap = new TreeMap<>();

    public Yields3(final List<Yield3> yields) {
        for (final Yield3 yield : yields) {
            yieldsMap.put(yield.alias, yield);
        }
    }

    public int size() {
        return yieldsMap.size();
    }
    
    public Collection<Yield3> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }

    public SortedMap<String, Yield3> getYieldsMap() {
        return unmodifiableSortedMap(yieldsMap);
    }

    public String sql(final EqlDomainMetadata metadata, final List<PropType> expectedTypes) {
        if (expectedTypes.size() != yieldsMap.size()) {
            throw new EqlStage3ProcessingException("""
                    Mismatch between number of yields and their expected types.
                    Yields: %s [%s]
                    Types : %s [%s].""".formatted(
                    yieldsMap.size(), CollectionUtil.toString(yieldsMap.values(), ", "),
                    expectedTypes.size(), CollectionUtil.toString(expectedTypes, ", ")));
        }

        return "SELECT\n" +
                zip(getYields().stream(), expectedTypes.stream(), (y, type) -> y.sql(metadata, type))
                        .collect(joining(", "));
    }

    public String sql(final EqlDomainMetadata metadata) {
        return "SELECT\n" + getYields().stream().map(y -> y.sql(metadata)).collect(joining(", "));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + yieldsMap.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Yields3)) {
            return false;
        }
        
        final Yields3 other = (Yields3) obj;

        return yieldsMap.equals(other.yieldsMap);
    }
}

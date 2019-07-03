package ua.com.fielden.platform.eql.stage3.elements;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Yields3 {
    private final SortedMap<String, Yield3> yieldsMap = new TreeMap<String, Yield3>();

    public Yields3(final List<Yield3> yields) {
        for (final Yield3 yield : yields) {
            yieldsMap.put(yield.alias, yield);
        }
    }
    
    public Collection<Yield3> getYields() {
        return Collections.unmodifiableCollection(yieldsMap.values());
    }

    public String sql(final DbVersion dbVersion) {
        return getYields().stream().map(y -> y.sql(dbVersion)).collect(joining(", "));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((yieldsMap == null) ? 0 : yieldsMap.hashCode());
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
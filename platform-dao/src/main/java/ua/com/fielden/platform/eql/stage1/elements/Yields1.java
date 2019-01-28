package ua.com.fielden.platform.eql.stage1.elements;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;

public class Yields1 {
    private final SortedMap<String, Yield1> yieldsMap = new TreeMap<String, Yield1>();

    public Yields1(List<Yield1> yields) {
        for (Yield1 yield : yields) {
            addYield(yield);
        }
    }
    
    public Yields2 transform(final TransformatorToS2 resolver) {
        final List<Yield2> yieldsList = new ArrayList<>(); 
        for (final Yield1 yield : yieldsMap.values()) {
            yieldsList.add(yield.transform(resolver));
        }
        return new Yields2(yieldsList);
    }

    public void addYield(final Yield1 yield) {
        if (yieldsMap.containsKey(yield.getAlias())) {
            throw new EqlStage1ProcessingException(format("Query contains duplicate yields for alias [%s].", yield.getAlias()));
        }
        yieldsMap.put(yield.getAlias(), yield);
    }

    public Collection<Yield1> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }

    @Override
    public String toString() {
        return yieldsMap.toString();
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Yields1)) {
            return false;
        }
        final Yields1 other = (Yields1) obj;
        if (yieldsMap == null) {
            if (other.yieldsMap != null) {
                return false;
            }
        } else if (!yieldsMap.equals(other.yieldsMap)) {
            return false;
        }
        return true;
    }
}
package ua.com.fielden.platform.eql.stage2.core;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSortedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.core.Yield3;
import ua.com.fielden.platform.eql.stage3.core.Yields3;

public class Yields2 {
    private final SortedMap<String, Yield2> yieldsMap = new TreeMap<String, Yield2>();

    public Yields2(final List<Yield2> yields) {
        for (final Yield2 yield : yields) {
            yieldsMap.put(yield.alias, yield);
        }
    }
    
    public Collection<Yield2> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }
    
    public SortedMap<String, Yield2> getYieldsMap() {
        return unmodifiableSortedMap(yieldsMap);
    }
    
    public TransformationResult<Yields3> transform(final TransformationContext context) {
        final List<Yield3> yieldsList = new ArrayList<>(); 
        TransformationContext currentContext = context;
        for (final Yield2 yield : yieldsMap.values()) {
            final TransformationResult<Yield3> yieldTr = yield.transform(currentContext);
            currentContext = yieldTr.updatedContext;
            yieldsList.add(yieldTr.item);
        }
        return new TransformationResult<Yields3>(new Yields3(yieldsList), currentContext);
    }
    
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        for (final Yield2 yield : yieldsMap.values()) {
            result.addAll(yield.operand.collectProps());
        }
        return result;
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

        if (!(obj instanceof Yields2)) {
            return false;
        }
        
        final Yields2 other = (Yields2) obj;

        return yieldsMap.equals(other.yieldsMap);
    }
}
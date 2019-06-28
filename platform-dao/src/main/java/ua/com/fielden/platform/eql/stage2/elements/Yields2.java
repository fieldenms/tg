package ua.com.fielden.platform.eql.stage2.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.stage3.elements.Yield3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;

public class Yields2 {
    private final SortedMap<String, Yield2> yieldsMap = new TreeMap<String, Yield2>();

    public Yields2(final List<Yield2> yields) {
        for (final Yield2 yield : yields) {
            yieldsMap.put(yield.alias, yield);
        }
    }
    
    public Collection<Yield2> getYields() {
        return Collections.unmodifiableCollection(yieldsMap.values());
    }
    
    public TransformationResult<Yields3> transform(final TransformationContext transformationContext) {
        final List<Yield3> yieldsList = new ArrayList<>(); 
        TransformationContext currentResolutionContext = transformationContext;
        for (final Yield2 yield : yieldsMap.values()) {
            final TransformationResult<Yield3> yieldTransformationResult = yield.transform(currentResolutionContext);
            currentResolutionContext = yieldTransformationResult.updatedContext;
            yieldsList.add(yieldTransformationResult.item);
        }
        return new TransformationResult<Yields3>(new Yields3(yieldsList), currentResolutionContext);
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

        if (!(obj instanceof Yields2)) {
            return false;
        }
        
        final Yields2 other = (Yields2) obj;

        return yieldsMap.equals(other.yieldsMap);
    }
}
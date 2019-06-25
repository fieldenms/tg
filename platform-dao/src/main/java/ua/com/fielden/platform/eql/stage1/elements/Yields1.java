package ua.com.fielden.platform.eql.stage1.elements;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;

public class Yields1 {
    private final SortedMap<String, Yield1> yieldsMap = new TreeMap<String, Yield1>();

    public Yields1(final List<Yield1> yields) {
        for (final Yield1 yield : yields) {
            addYield(yield);
        }
    }
    
    public TransformationResult<Yields2> transform(final PropsResolutionContext resolutionContext) {
        final List<Yield2> yieldsList = new ArrayList<>(); 
        PropsResolutionContext currentResolutionContext = resolutionContext;
        for (final Yield1 yield : yieldsMap.values()) {
            final TransformationResult<Yield2> yieldTransformationResult = yield.transform(currentResolutionContext);
            currentResolutionContext = yieldTransformationResult.getUpdatedContext();
            yieldsList.add(yieldTransformationResult.getItem());
        }
        return new TransformationResult<Yields2>(new Yields2(yieldsList), currentResolutionContext);
    }

    public void addYield(final Yield1 yield) {
        if (yieldsMap.containsKey(yield.alias)) {
            throw new EqlStage1ProcessingException(format("Query contains duplicate yields for alias [%s].", yield.alias));
        }
        yieldsMap.put(yield.alias, yield);
    }

    public Collection<Yield1> getYields() {
        return unmodifiableCollection(yieldsMap.values());
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

        if (!(obj instanceof Yields1)) {
            return false;
        }
        
        final Yields1 other = (Yields1) obj;
        
        return yieldsMap.equals(other.yieldsMap);
    }
}
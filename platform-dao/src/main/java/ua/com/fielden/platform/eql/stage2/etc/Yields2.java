package ua.com.fielden.platform.eql.stage2.etc;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.stage0.YieldBuilder.ABSENT_ALIAS;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;
import ua.com.fielden.platform.eql.stage3.etc.Yield3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;

public class Yields2 {
    public static final Yields2 EMPTY_YIELDS = new Yields2(emptyList());
    
    private final SortedMap<String, Yield2> yieldsMap = new TreeMap<String, Yield2>();
    public final boolean allGenerated;
    
    public static Yields2 nullYields = new Yields2(listOf(new Yield2(new Value2(null), ABSENT_ALIAS, false))); 

    public Yields2(final List<Yield2> yields, final boolean allGenerated) {
        this.allGenerated = allGenerated;
        for (final Yield2 yield : yields) {
            yieldsMap.put(yield.alias, yield);
        }
    }

    public Yields2(final List<Yield2> yields) {
        this(yields, false);
    }

    public Collection<Yield2> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }
    
    public SortedMap<String, Yield2> getYieldsMap() {
        return unmodifiableSortedMap(yieldsMap);
    }
    
    public TransformationResult2<Yields3> transform(final TransformationContext2 context) {
        final List<Yield3> yieldsList = new ArrayList<>(); 
        TransformationContext2 currentContext = context;
        for (final Yield2 yield : yieldsMap.values()) {
            final TransformationResult2<Yield3> yieldTr = yield.transform(currentContext);
            currentContext = yieldTr.updatedContext;
            yieldsList.add(yieldTr.item);
        }
        return new TransformationResult2<>(new Yields3(yieldsList), currentContext);
    }
    
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final Yield2 yield : yieldsMap.values()) {
            result.addAll(yield.operand.collectProps());
        }
        return result;
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return yieldsMap.isEmpty() ? emptySet() : yieldsMap.values().stream().map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + yieldsMap.hashCode();
        result = prime * result + (allGenerated ? 1231 : 1237);
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

        return yieldsMap.equals(other.yieldsMap) && (allGenerated == other.allGenerated);
    }
}
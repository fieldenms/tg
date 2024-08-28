package ua.com.fielden.platform.eql.stage2.sundries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Collections.*;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.stage1.sundries.Yield1.ABSENT_ALIAS;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

public record Yields2 (SortedMap<String, Yield2> yieldsMap, boolean allGenerated) implements ToString.IFormattable {

    public static final Yields2 EMPTY_YIELDS = new Yields2(emptyList());
    
    public static Yields2 nullYields = new Yields2(listOf(new Yield2(new Value2(null), ABSENT_ALIAS, false)));

    public Yields2(final List<Yield2> yields, final boolean allGenerated) {
        this(yields.stream().collect(toImmutableSortedMap(naturalOrder(), Yield2::alias, Function.identity())),
             allGenerated);
    }

    public Yields2(final List<Yield2> yields) {
        this(yields, false);
    }

    public boolean isEmpty() {
        return yieldsMap.isEmpty();
    }

    public Collection<Yield2> getYields() {
        return unmodifiableCollection(yieldsMap.values());
    }
    
    public SortedMap<String, Yield2> getYieldsMap() {
        return unmodifiableSortedMap(yieldsMap);
    }
    
    public TransformationResultFromStage2To3<Yields3> transform(final TransformationContextFromStage2To3 context) {
        final List<Yield3> yieldsList = new ArrayList<>(); 
        TransformationContextFromStage2To3 currentContext = context;
        for (final Yield2 yield : yieldsMap.values()) {
            final TransformationResultFromStage2To3<Yield3> yieldTr = yield.transform(currentContext);
            currentContext = yieldTr.updatedContext;
            yieldsList.add(yieldTr.item);
        }
        return new TransformationResultFromStage2To3<>(new Yields3(yieldsList), currentContext);
    }
    
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final Yield2 yield : yieldsMap.values()) {
            result.addAll(yield.operand().collectProps());
        }
        return result;
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return yieldsMap.isEmpty() ? emptySet() : yieldsMap.values().stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yields", yieldsMap)
                .add("allGenerated", allGenerated)
                .$();
    }

}

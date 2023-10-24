package ua.com.fielden.platform.eql.stage1.etc;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;

public class Yields1 {
    public static final Yields1 EMPTY_YIELDS = new Yields1(emptyList());

    private final SortedMap<String, Yield1> yieldsMap = new TreeMap<String, Yield1>();

    public Yields1(final List<Yield1> yields) {
        for (final Yield1 yield : yields) {
            addYield(yield);
        }
    }

    public Yields2 transform(final TransformationContext1 context) {
        return yieldsMap.isEmpty() ? Yields2.EMPTY_YIELDS : new Yields2(yieldsMap.values().stream().map(el -> el.transform(context)).collect(toList()));
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

    public SortedMap<String, Yield1> getYieldsMap() {
        return unmodifiableSortedMap(yieldsMap);
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return yieldsMap.isEmpty() ? emptySet() : yieldsMap.values().stream().map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
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

        if (!(obj instanceof Yields1)) {
            return false;
        }

        final Yields1 other = (Yields1) obj;

        return yieldsMap.equals(other.yieldsMap);
    }
}
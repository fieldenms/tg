package ua.com.fielden.platform.eql.meta.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableSortedMap;

public class QuerySourceItemForComponentType<T> extends AbstractQuerySourceItem<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractQuerySourceItem<?>> subitems = new TreeMap<>(); // TODO why sorted?

    public QuerySourceItemForComponentType(final String name, final Class<T> javaType, final @Nullable Object hibType) {
        super(name, hibType, null);
        this.javaType = javaType;
    }

    @Override
    public AbstractQuerySourceItem<T> cloneWithoutExpression() {
        final QuerySourceItemForComponentType<T> result = new QuerySourceItemForComponentType<T>(name, javaType, hibType);
        for (final Entry<String, AbstractQuerySourceItem<?>> entry : subitems.entrySet()) {
            result.subitems.put(entry.getKey(), entry.getValue().cloneWithoutExpression());
        }
        return result;
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, subitems);
    }

    public QuerySourceItemForComponentType<T> addSubitem(final AbstractQuerySourceItem<?> subitem) {
        subitems.put(subitem.name, subitem);
        return this;
    }

    public SortedMap<String, AbstractQuerySourceItem<?>> getSubitems() {
        return unmodifiableSortedMap(subitems);
    }

    @Override
    public boolean hasExpression() {
        return subitems.values().stream().anyMatch(p -> p.hasExpression());
    }

    @Override
    public boolean isCalculatedPropertyThatCouldBeMaterialisedAsSqlColumn() {
        return subitems.values().stream().anyMatch(p -> p.isCalculatedPropertyThatCouldBeMaterialisedAsSqlColumn());
    }

    @Override
    public Class<T> javaType() {
        return javaType;
    }

    @Override
    public String toString() {
        return String.format("%20s %20s", name, javaType.getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + javaType.hashCode();
        result = prime * result + subitems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof QuerySourceItemForComponentType)) {
            return false;
        }

        final QuerySourceItemForComponentType<?> other = (QuerySourceItemForComponentType<?>) obj;

        return Objects.equals(subitems, other.subitems) && Objects.equals(javaType, other.javaType);
    }
}

package ua.com.fielden.platform.eql.meta.query;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public class ComponentTypeQuerySourceInfoItem<T> extends AbstractQuerySourceInfoItem<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractQuerySourceInfoItem<?>> subitems = new TreeMap<>(); // TODO why sorted?

    public ComponentTypeQuerySourceInfoItem(final String name, final Class<T> javaType, final Object hibType) {
        super(name, hibType, null);
        this.javaType = javaType;
    }
    
    @Override
    public AbstractQuerySourceInfoItem<T> cloneWithoutExpression() {
        final ComponentTypeQuerySourceInfoItem<T> result = new ComponentTypeQuerySourceInfoItem<T>(name, javaType, hibType);
        for (final Entry<String, AbstractQuerySourceInfoItem<?>> entry : subitems.entrySet()) {
            result.subitems.put(entry.getKey(), entry.getValue().cloneWithoutExpression());
        }
        return result;
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, subitems);
    }
    
    public ComponentTypeQuerySourceInfoItem<T> addSubitem(final AbstractQuerySourceInfoItem<?> subitem) { 
        subitems.put(subitem.name, subitem);
        return this;
    }
    
    public SortedMap<String, AbstractQuerySourceInfoItem<?>> getSubitems() {
        return unmodifiableSortedMap(subitems);
    }
    
    @Override
    public boolean hasExpression() {
        return subitems.values().stream().anyMatch(p -> p.hasExpression());
    }
    
    @Override
    public boolean hasAggregation() {
        return subitems.values().stream().anyMatch(p -> p.hasAggregation());
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

        if (!(obj instanceof ComponentTypeQuerySourceInfoItem)) {
            return false;
        }

        final ComponentTypeQuerySourceInfoItem<?> other = (ComponentTypeQuerySourceInfoItem<?>) obj;

        return Objects.equals(subitems, other.subitems) && Objects.equals(javaType, other.javaType);
    }
}
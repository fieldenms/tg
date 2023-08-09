package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public class QuerySourceInfo<T extends AbstractEntity<?>> implements IResolvable<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractPropInfo<?>> props = new TreeMap<>();
    public final boolean isComprehensive; //indicates that all data-backed props from PE/SE are present

    public QuerySourceInfo(final Class<T> javaType, final boolean isComprehensive) {
        this.javaType = javaType;
        this.isComprehensive = isComprehensive;
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, props);
    }

    public QuerySourceInfo<T> addProp(final AbstractPropInfo<?> propInfo) { 
        props.put(propInfo.name, propInfo);
        return this;
    }
    
    public SortedMap<String, AbstractPropInfo<?>> getProps() {
        return unmodifiableSortedMap(props);
    }
    
    @Override
    public String toString() {
        return javaType.getSimpleName();
    }

    @Override
    public Class<T> javaType() {
        return javaType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + javaType.hashCode();
        result = prime * result + props.keySet().hashCode();
        result = prime * result + (isComprehensive ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QuerySourceInfo)) {
            return false;
        }

        final QuerySourceInfo<?> other = (QuerySourceInfo<?>) obj;

        return Objects.equals(javaType, other.javaType) && Objects.equals(props.keySet(), other.props.keySet()) && isComprehensive == other.isComprehensive;
    }
}
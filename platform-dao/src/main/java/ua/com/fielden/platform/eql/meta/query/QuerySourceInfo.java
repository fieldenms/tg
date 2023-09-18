package ua.com.fielden.platform.eql.meta.query;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Collection;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public class QuerySourceInfo<T extends AbstractEntity<?>> implements IResolvable<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractPropInfo<?>> propsMap = new TreeMap<>();
    public final boolean isComprehensive; //indicates that all data-backed props from PE/SE are present

    public QuerySourceInfo(final Class<T> javaType, final boolean isComprehensive) {
        this.javaType = javaType;
        this.isComprehensive = isComprehensive;
    }

    public QuerySourceInfo(final Class<T> javaType, final boolean isComprehensive, final Collection<AbstractPropInfo<?>> props) {
        this(javaType, isComprehensive);
        addProps(props);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, propsMap);
    }

    public void addProps(final Collection<AbstractPropInfo<?>> props) {
        for (final AbstractPropInfo<?> prop : props) {
            propsMap.put(prop.name, prop);    
        }
    }
    
    public SortedMap<String, AbstractPropInfo<?>> getProps() {
        return unmodifiableSortedMap(propsMap);
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
        result = prime * result + propsMap.keySet().hashCode();
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

        return Objects.equals(javaType, other.javaType) && Objects.equals(propsMap.keySet(), other.propsMap.keySet()) && isComprehensive == other.isComprehensive;
    }
}
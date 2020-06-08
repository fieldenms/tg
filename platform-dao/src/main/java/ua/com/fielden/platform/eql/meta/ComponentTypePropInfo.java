package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class ComponentTypePropInfo<T> extends AbstractPropInfo<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractPropInfo<?>> props = new TreeMap<>();

    public ComponentTypePropInfo(final String name, final Class<T> javaType, final Object hibType) {
        super(name, hibType, null);
        this.javaType = javaType;
    }

    @Override
    public ResolutionContext resolve(final ResolutionContext context) {
        if (context.isSuccessful()) {
            return context;
        } else {
            final AbstractPropInfo<?> foundPart = props.get(context.getNextPending());
            return foundPart == null ? context : foundPart.resolve(context.registerResolutionAndClone(foundPart));
        }
    }
    
    public ComponentTypePropInfo<T> addProp(final AbstractPropInfo<?> propInfo) { 
        props.put(propInfo.name, propInfo);
        return this;
    }
    
    public SortedMap<String, AbstractPropInfo<?>> getProps() {
        return unmodifiableSortedMap(props);
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
        result = prime * result + props.hashCode();
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

        if (!(obj instanceof UnionTypePropInfo)) {
            return false;
        }

        final ComponentTypePropInfo other = (ComponentTypePropInfo) obj;

        return Objects.equals(props, other.props) && Objects.equals(javaType, other.javaType);
    }
}
package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

/**
 * A structure that captures a query source yield-able entity-typed-property resolution related info within a query source of type <code>PARENT</code>.
 * 
 * @author TG Team
 *
 * @param <T>
 * @param <PARENT>
 */
public class UnionTypePropInfo<T extends AbstractUnionEntity> extends AbstractPropInfo<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractPropInfo<?>> props = new TreeMap<>(); // TODO why sorted?

    public UnionTypePropInfo(final String name, final Class<T> javaType, final Object hibType, final SortedMap<String, AbstractPropInfo<?>> props) {
        super(name, hibType, null);
        this.javaType = javaType;
        this.props.putAll(props);
    }
    
    @Override
    public AbstractPropInfo<T> cloneWithoutExpression() {
        return new UnionTypePropInfo<T>(name, javaType, hibType, props);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        if (context.isSuccessful()) {
            return context;
        } else {
            final AbstractPropInfo<?> foundPart = props.get(context.getNextPending());
            return foundPart == null ? context : foundPart.resolve(context.registerResolutionAndClone(foundPart));
        }
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

        final UnionTypePropInfo<?> other = (UnionTypePropInfo<?>) obj;

        return Objects.equals(props, other.props) && Objects.equals(javaType, other.javaType);
    }
}
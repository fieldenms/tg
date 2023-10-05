package ua.com.fielden.platform.eql.meta.query;

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
public class UnionTypeQuerySourceInfoItem<T extends AbstractUnionEntity> extends AbstractQuerySourceInfoItem<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractQuerySourceInfoItem<?>> subitems = new TreeMap<>(); // TODO why sorted?

    public UnionTypeQuerySourceInfoItem(final String name, final Class<T> javaType, final Object hibType, final SortedMap<String, AbstractQuerySourceInfoItem<?>> props) {
        super(name, hibType, null);
        this.javaType = javaType;
        this.subitems.putAll(props);
    }
    
    @Override
    public AbstractQuerySourceInfoItem<T> cloneWithoutExpression() {
        return new UnionTypeQuerySourceInfoItem<T>(name, javaType, hibType, subitems);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, subitems);
    }
    
    public SortedMap<String, AbstractQuerySourceInfoItem<?>> getProps() {
        return unmodifiableSortedMap(subitems);
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

        if (!(obj instanceof UnionTypeQuerySourceInfoItem)) {
            return false;
        }

        final UnionTypeQuerySourceInfoItem<?> other = (UnionTypeQuerySourceInfoItem<?>) obj;

        return Objects.equals(subitems, other.subitems) && Objects.equals(javaType, other.javaType);
    }
}
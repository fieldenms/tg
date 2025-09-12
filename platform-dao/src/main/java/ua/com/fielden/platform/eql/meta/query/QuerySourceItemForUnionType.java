package ua.com.fielden.platform.eql.meta.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableSortedMap;

/**
 * A structure that represents a query source item of union type {@code T}.
 *
 * @author TG Team
 */
public class QuerySourceItemForUnionType<T extends AbstractUnionEntity> extends AbstractQuerySourceItem<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractQuerySourceItem<?>> subitems = new TreeMap<>(); // TODO why sorted?

    public QuerySourceItemForUnionType(
            final String name,
            final Class<T> javaType,
            final @Nullable Object hibType,
            final SortedMap<String, AbstractQuerySourceItem<?>> props)
    {
        super(name, hibType, null);
        this.javaType = javaType;
        this.subitems.putAll(props);
    }

    @Override
    public AbstractQuerySourceItem<T> cloneWithoutExpression() {
        return new QuerySourceItemForUnionType<T>(name, javaType, hibType, subitems);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, subitems);
    }

    public SortedMap<String, AbstractQuerySourceItem<?>> getProps() {
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

        if (!(obj instanceof QuerySourceItemForUnionType)) {
            return false;
        }

        final QuerySourceItemForUnionType<?> other = (QuerySourceItemForUnionType<?>) obj;

        return Objects.equals(subitems, other.subitems) && Objects.equals(javaType, other.javaType);
    }
}

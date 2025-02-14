package ua.com.fielden.platform.eql.meta.query;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.Collection;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

/**
 * A structure to describe where an EQL query gets the data from, which is the query source (entity type or another query/s).
 * It is essential for dot-expression processing within a query.
 *
 * @param <T> -- an entity type, represented be the structure.
 *
 * @author TG Team
 */
public class QuerySourceInfo<T extends AbstractEntity<?>> implements IResolvable<T> {

    /**
     * The type of this query source (either persistent entity, synthetic entity, union entity or entity aggregates).
     */
    private final Class<T> javaType;

    /**
     * Association between simple property names and their corresponding query source items.
     * The use of a sorted map is for convenience only (e.g., for unit testing).
     */
    private final SortedMap<String, AbstractQuerySourceItem<?>> propsMap = new TreeMap<>();

    /**
     * Indicates whether this query source info is <i>comprehensive</i>, the meaning of which depends on the entity type:
     * <ul>
     *   <li> For a persistent entity - all retrievable properties are represented by this query source info.
     *   <li> For a synthetic entity - all properties present in the underlying model are represented by this query source info.
     * </ul>
     * In other words, if a query source info is comprehensive, then it is a canonical representation of the respective data source.
     */
    public final boolean isComprehensive;

    public QuerySourceInfo(final Class<T> javaType, final boolean isComprehensive) {
        this.javaType = javaType;
        this.isComprehensive = isComprehensive;
    }

    public QuerySourceInfo(final Class<T> javaType, final boolean isComprehensive, final Collection<AbstractQuerySourceItem<?>> props) {
        this(javaType, isComprehensive);
        addProps(props);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return IResolvable.resolve(context, propsMap);
    }

    public void addProps(final Collection<AbstractQuerySourceItem<?>> props) {
        for (final AbstractQuerySourceItem<?> prop : props) {
            propsMap.put(prop.name, prop);
        }
    }

    public SortedMap<String, AbstractQuerySourceItem<?>> getProps() {
        return unmodifiableSortedMap(propsMap);
    }

    public boolean hasProp(final String name) {
        return propsMap.containsKey(name);
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
        result = prime * result + propsMap.keySet().hashCode(); // keySet is used because values can be recursively defined.
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

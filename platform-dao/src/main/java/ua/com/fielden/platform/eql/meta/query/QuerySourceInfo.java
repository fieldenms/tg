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
 * It is essential for dot-notation processing within a query.
 *
 * @param <T> -- an entity type, represented be the structure.
 *
 * @author TG Team
 */
public class QuerySourceInfo<T extends AbstractEntity<?>> implements IResolvable<T> {

    /**
     * The type of a query source (either persistent entity, synthetic entity, union entity or entity aggregates).
     */
    private final Class<T> javaType;

    /**
     * A map between java class field name representing a property and a corresponding query source item.
     * The use of a sorted map is for convenience only (e.g., for unit testing).
     */
    private final SortedMap<String, AbstractQuerySourceItem<?>> propsMap = new TreeMap<>();

    /**
     * Used to indicate that all data-backed props from PE/SE are present in this query source info.
     * In other words, it is a canonical representation (all properties) of the data source for a persistent entity (PE) or a synthetic entity (SE).
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
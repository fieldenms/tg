package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExistenceTest extends AbstractCondition {
    private final boolean negated;
    private final EntQuery subQuery;

    public ExistenceTest(final boolean negated, final EntQuery subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public String sql() {
        return (negated ? "NOT EXISTS " : "EXISTS ") + subQuery.sql();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return Arrays.asList(new EntQuery[] { subQuery });
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExistenceTest)) {
            return false;
        }
        final ExistenceTest other = (ExistenceTest) obj;
        if (negated != other.negated) {
            return false;
        }
        if (subQuery == null) {
            if (other.subQuery != null) {
                return false;
            }
        } else if (!subQuery.equals(other.subQuery)) {
            return false;
        }
        return true;
    }

    @Override
    protected List<IPropertyCollector> getCollection() {
        return new ArrayList<IPropertyCollector>() {
            {
                add(subQuery);
            }
        };
    }
}
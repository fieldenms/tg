package ua.com.fielden.platform.eql.s2.elements;

import java.util.Map;

import ua.com.fielden.platform.entity.query.FetchModel;
import ua.com.fielden.platform.eql.meta.QueryCategory;

public class EntQuery2 implements ISingleOperand2 {

    private final Sources2 sources;
    private final Conditions2 conditions;
    private final Yields2 yields;
    private final GroupBys2 groups;
    private final OrderBys2 orderings;
    private final Class resultType;
    private final QueryCategory category;
    private final Map<String, Object> paramValues;

    public Class type() {
        return resultType;
    }

    public EntQuery2(final EntQueryBlocks2 queryBlocks, final Class resultType, final QueryCategory category, //
	    final FetchModel fetchModel, final Map<String, Object> paramValues) {
        super();
        this.category = category;
        this.sources = queryBlocks.getSources();
        this.conditions = queryBlocks.getConditions();
        this.yields = queryBlocks.getYields();
        this.groups = queryBlocks.getGroups();
        this.orderings = queryBlocks.getOrderings();
        this.resultType = resultType;
        if (this.resultType == null && category != QueryCategory.SUB_QUERY) { // only primitive result queries have result type not assigned
            throw new IllegalStateException("This query is not subquery, thus its result type shouldn't be null!");
        }

        this.paramValues = paramValues;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    public Sources2 getSources() {
        return sources;
    }

    public Conditions2 getConditions() {
        return conditions;
    }

    public Yields2 getYields() {
        return yields;
    }

    public GroupBys2 getGroups() {
        return groups;
    }

    public OrderBys2 getOrderings() {
        return orderings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + ((yields == null) ? 0 : yields.hashCode());
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
        if (!(obj instanceof EntQuery2)) {
            return false;
        }
        final EntQuery2 other = (EntQuery2) obj;
        if (conditions == null) {
            if (other.conditions != null) {
                return false;
            }
        } else if (!conditions.equals(other.conditions)) {
            return false;
        }
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (category != other.category) {
            return false;
        }
        if (resultType == null) {
            if (other.resultType != null) {
                return false;
            }
        } else if (!resultType.equals(other.resultType)) {
            return false;
        }
        if (sources == null) {
            if (other.sources != null) {
                return false;
            }
        } else if (!sources.equals(other.sources)) {
            return false;
        }
        if (yields == null) {
            if (other.yields != null) {
                return false;
            }
        } else if (!yields.equals(other.yields)) {
            return false;
        }
        return true;
    }
}
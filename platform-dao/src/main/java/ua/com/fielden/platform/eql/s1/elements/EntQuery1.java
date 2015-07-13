package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s1.processing.EntQueryBlocks1;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.s2.elements.ISource2;

public class EntQuery1 implements ISingleOperand1<EntQuery2> {

    private final Sources1 sources;
    private final Conditions1 conditions;
    private final Yields1 yields;
    private final GroupBys1 groups;
    private final OrderBys1 orderings;

    private final Class resultType;
    private final QueryCategory category;
    private final IRetrievalModel fetchModel;

    private final boolean filterable;

    private boolean isSubQuery() {
        return QueryCategory.SUB_QUERY.equals(category);
    }

    public Class type() {
        return resultType;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(isSubQuery() ? "(" : "");
        sb.append("SELECT ");
        sb.append(yields);
        sb.append("\nFROM ");
        sb.append(sources);
        sb.append("\nWHERE ");
        sb.append(conditions);
        sb.append(groups);
        sb.append(isSubQuery() ? ")" : "");
        sb.append(orderings);
        return sb.toString();
    }

    @Override
    public EntQuery2 transform(final TransformatorToS2 resolver) {
        final TransformatorToS2 localResolver = resolver.produceBasedOn();

        for (final ISource1<? extends ISource2> source : sources.getAllSources()) {
            localResolver.transformAndAccumulateSource(source);
        }

        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sources.transform(localResolver), conditions/*enhancedConditions*/.transform(localResolver), //
        yields.transform(localResolver), groups.transform(localResolver), orderings.transform(localResolver));

        return new EntQuery2(entQueryBlocks, resultType, category, fetchModel);
    }

    public EntQuery1(final EntQueryBlocks1 queryBlocks, final Class resultType, final QueryCategory category, //
            final IRetrievalModel fetchModel, final boolean filterable) {
        super();
        this.filterable = filterable;
        this.category = category;
        this.fetchModel = fetchModel;
        this.sources = queryBlocks.getSources();
        this.conditions = queryBlocks.getConditions();
        this.yields = queryBlocks.getYields();
        this.groups = queryBlocks.getGroups();
        this.orderings = queryBlocks.getOrderings();
        this.resultType = resultType;
        if (this.resultType == null && category != QueryCategory.SUB_QUERY) { // only primitive result queries have result type not assigned
            throw new IllegalStateException("This query is not subquery, thus its result type shouldn't be null!");
        }
    }

    public Sources1 getSources() {
        return sources;
    }

    public Conditions1 getConditions() {
        return conditions;
    }

    public Yields1 getYields() {
        return yields;
    }

    public GroupBys1 getGroups() {
        return groups;
    }

    public OrderBys1 getOrderings() {
        return orderings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((fetchModel == null) ? 0 : fetchModel.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
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
        if (!(obj instanceof EntQuery1)) {
            return false;
        }
        final EntQuery1 other = (EntQuery1) obj;
        if (category != other.category) {
            return false;
        }
        if (conditions == null) {
            if (other.conditions != null) {
                return false;
            }
        } else if (!conditions.equals(other.conditions)) {
            return false;
        }
        if (fetchModel == null) {
            if (other.fetchModel != null) {
                return false;
            }
        } else if (!fetchModel.equals(other.fetchModel)) {
            return false;
        }
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (orderings == null) {
            if (other.orderings != null) {
                return false;
            }
        } else if (!orderings.equals(other.orderings)) {
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
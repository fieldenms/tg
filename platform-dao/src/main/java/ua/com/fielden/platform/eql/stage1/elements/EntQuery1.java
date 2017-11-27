package ua.com.fielden.platform.eql.stage1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryBlocks;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneConditionBuilder;
import ua.com.fielden.platform.eql.stage2.elements.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;

public class EntQuery1 implements ISingleOperand1<EntQuery2> {

    private final Sources1 sources;
    private final Conditions1 conditions;
    private final Yields1 yields;
    private final GroupBys1 groups;
    private final OrderBys1 orderings;

    private final Class<? extends AbstractEntity<?>> resultType;
    private final QueryCategory category;

    private final boolean filterable;

    private boolean isSubQuery() {
        return QueryCategory.SUB_QUERY.equals(category);
    }

    public Class<? extends AbstractEntity<?>> type() {
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

    private Conditions1 enhanceConditions(final Conditions1 originalConditions, final IFilter filter, //
            final String username, final IQrySource1<? extends IQrySource2> mainSource, final EntQueryGenerator generator) {
        if (mainSource instanceof QrySource1BasedOnPersistentType && filter != null) {
            final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), mainSource.getAlias(), username);
            if (filteringCondition == null) {
                return originalConditions;
            }
            //logger.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() +"]");
            final List<CompoundCondition1> others = new ArrayList<>();
            others.add(new CompoundCondition1(LogicalOperator.AND, originalConditions));
            final Conditions1 filteringConditions = new StandAloneConditionBuilder(generator, filteringCondition, false).getModel();
            return originalConditions.isEmpty() ? filteringConditions : new Conditions1(false, filteringConditions, others);
        } else {
            return originalConditions;
        }
    }

    @Override
    public EntQuery2 transform(final TransformatorToS2 resolver) {
        final TransformatorToS2 localResolver = resolver.produceBasedOn();

        // TODO Need to resolve joinConditions of each CompoundSource as soon as it is added to resolver.  
        for (final IQrySource1<? extends IQrySource2> source : sources.getAllSources()) {
            localResolver.addSource(source);
        }

        final Conditions1 enhancedConditions = enhanceConditions(conditions, resolver.getFilter(), resolver.getUsername(), sources.getMain(), resolver.getEntQueryGenerator1());
        // TODO As part of transforming sources need to retrieve already resolved joinConditions, that happened while invoking addSource method (refer TODO above).
        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sources.transform(localResolver), enhancedConditions.transform(localResolver), //
        yields.transform(localResolver), groups.transform(localResolver), orderings.transform(localResolver));

        return new EntQuery2(entQueryBlocks, resultType, category);
    }

    public EntQuery1(final EntQueryBlocks queryBlocks, final Class resultType, final QueryCategory category, //
             final boolean filterable) {
        this.filterable = filterable;
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
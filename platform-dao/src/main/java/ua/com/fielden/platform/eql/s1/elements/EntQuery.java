package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.FetchModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.s1.processing.EntQueryBlocks;
import ua.com.fielden.platform.eql.s1.processing.EntQueryGenerator;
import ua.com.fielden.platform.eql.s1.processing.StandAloneConditionBuilder;

public class EntQuery implements ISingleOperand<ua.com.fielden.platform.eql.s2.elements.EntQuery> {

    private final boolean persistedType;
    private final Sources sources;
    private final Conditions conditions;
    private final Yields yields;
    private final GroupBys groups;
    private final OrderBys orderings;
    private final Class resultType;
    private final QueryCategory category;
    private final DomainMetadataAnalyser domainMetadataAnalyser;
    private final Map<String, Object> paramValues;

    transient private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public ua.com.fielden.platform.eql.s2.elements.EntQuery transform() {
	// TODO Auto-generated method stub
	return null;
    }


    private boolean isSubQuery() {
        return QueryCategory.SUB_QUERY.equals(category);
    }

    private boolean isSourceQuery() {
        return QueryCategory.SOURCE_QUERY.equals(category);
    }

    private boolean isResultQuery() {
        return QueryCategory.RESULT_QUERY.equals(category);
    }

    private boolean mainSourceIsTypeBased() {
	return getSources().getMain() instanceof TypeBasedSource;
    }

    private boolean mainSourceIsQueryBased() {
	return getSources().getMain() instanceof QueryBasedSource;
    }

    private Conditions enhanceConditions(final Conditions originalConditions, final IFilter filter, final String username, final ISource mainSource, final EntQueryGenerator generator, final Map<String, Object> paramValues) {
	if (mainSource instanceof TypeBasedSource && filter != null) {
	final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), mainSource.getAlias(), username);
	if (filteringCondition == null) {
	    return originalConditions;
	}
	logger.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() +"]");
	final List<CompoundCondition> others = new ArrayList();
	others.add(new CompoundCondition(LogicalOperator.AND, originalConditions));
	return originalConditions.ignore() ? new Conditions(new StandAloneConditionBuilder(generator, paramValues, filteringCondition, false).getModel()) : new Conditions(new StandAloneConditionBuilder(generator, paramValues, filteringCondition, false).getModel(), others);
	} else {
	    return originalConditions;
	}
    }

    public EntQuery(final boolean filterable, final EntQueryBlocks queryBlocks, final Class resultType, final QueryCategory category, //
	    final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username, //
            final EntQueryGenerator generator, final FetchModel fetchModel, final Map<String, Object> paramValues) {
        super();
        this.category = category;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.sources = queryBlocks.getSources();
        this.conditions = filterable ? enhanceConditions(queryBlocks.getConditions(), filter, username, sources.getMain(), generator, paramValues) : queryBlocks.getConditions();
        this.yields = queryBlocks.getYields();
        this.groups = queryBlocks.getGroups();
        this.orderings = queryBlocks.getOrderings();
        this.resultType = resultType;// != null ? resultType : (yields.size() == 0 ? this.sources.getMain().sourceType() : null);
        if (this.resultType == null && category != QueryCategory.SUB_QUERY) { // only primitive result queries have result type not assigned
            throw new IllegalStateException("This query is not subquery, thus its result type shouldn't be null!");
        }

        persistedType = (resultType == null || resultType == EntityAggregates.class) ? false : domainMetadataAnalyser.getEntityMetadata(this.resultType).isPersisted();

        this.paramValues = paramValues;
    }

    /**
     * By immediate props here are meant props used within this query and not within it's (nested) subqueries.
     *
     * @return
     */
    public List<EntProp> getImmediateProps() {
        final List<EntProp> result = new ArrayList<EntProp>();
        result.addAll(sources.getLocalProps());
        result.addAll(conditions.getLocalProps());
        result.addAll(groups.getLocalProps());
        result.addAll(yields.getLocalProps());
        result.addAll(orderings.getLocalProps());
        return result;
    }

    public List<EntQuery> getImmediateSubqueries() {
        final List<EntQuery> result = new ArrayList<EntQuery>();
        result.addAll(yields.getLocalSubQueries());
        result.addAll(groups.getLocalSubQueries());
        result.addAll(orderings.getLocalSubQueries());
        result.addAll(conditions.getLocalSubQueries());
        result.addAll(sources.getLocalSubQueries());
        return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
        return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return Arrays.asList(new EntQuery[] { this });
    }

    @Override
    public List<EntValue> getAllValues() {
        final List<EntValue> result = new ArrayList<EntValue>();
        result.addAll(sources.getAllValues());
        result.addAll(conditions.getAllValues());
        result.addAll(groups.getAllValues());
        result.addAll(orderings.getAllValues());
        result.addAll(yields.getAllValues());
        return result;
    }

    public Sources getSources() {
        return sources;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public Yields getYields() {
        return yields;
    }

    public GroupBys getGroups() {
        return groups;
    }

    public OrderBys getOrderings() {
        return orderings;
    }

    @Override
    public boolean ignore() {
        return false;
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
        if (!(obj instanceof EntQuery)) {
            return false;
        }
        final EntQuery other = (EntQuery) obj;
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

    public boolean isPersistedType() {
        return persistedType;
    }
}
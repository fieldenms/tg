package ua.com.fielden.platform.eql.s1.elements;

import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.FetchModel;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s1.processing.EntQueryBlocks1;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.s2.elements.ISource2;

public class EntQuery1 implements ISingleOperand1<EntQuery2> {

    private final boolean persistedType;
    private final Sources1 sources;
    private final Conditions1 conditions;
    private final Yields1 yields;
    private final GroupBys1 groups;
    private final OrderBys1 orderings;
    private final Class resultType;
    private final QueryCategory category;
    private final DomainMetadataAnalyser domainMetadataAnalyser;
    private final Map<String, Object> paramValues;

    transient private final Logger logger = Logger.getLogger(this.getClass());

    private boolean isSubQuery() {
        return QueryCategory.SUB_QUERY.equals(category);
    }


    public Class type() {
        return resultType;
    }

    @Override
    public String toString() {
        return sql();
    }

    public String sql() {
        final StringBuffer sb = new StringBuffer();
        sb.append(isSubQuery() ? "(" : "");
        sb.append("SELECT ");
        sb.append(yields);
        sb.append("\nFROM ");
        sb.append(sources);
        if (!conditions.ignore()) {
            sb.append("\nWHERE ");
            sb.append(conditions);
        }
        sb.append(groups);
        sb.append(isSubQuery() ? ")" : "");
        sb.append(orderings);
        return sb.toString();
    }

    @Override
    public EntQuery2 transform(final TransformatorToS2 resolver) {
	final TransformatorToS2 localResolver = resolver.produceBasedOn();

	for (final ISource1<? extends ISource2> source : sources.getAllSources()) {
	    localResolver.addSource(source);
	}

	final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sources.transform(localResolver), conditions.transform(localResolver), //
		yields.transform(localResolver), groups.transform(localResolver), orderings.transform(localResolver));

	return new EntQuery2(false, entQueryBlocks, resultType, category, domainMetadataAnalyser, null, null, null, null, paramValues);
    }

    public EntQuery1(final EntQueryBlocks1 queryBlocks, final Class resultType, final QueryCategory category, //
	    final DomainMetadataAnalyser domainMetadataAnalyser, final FetchModel fetchModel, final Map<String, Object> paramValues) {
        super();
        this.category = category;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.sources = queryBlocks.getSources();
        this.conditions = queryBlocks.getConditions();
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
        if (!(obj instanceof EntQuery1)) {
            return false;
        }
        final EntQuery1 other = (EntQuery1) obj;
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
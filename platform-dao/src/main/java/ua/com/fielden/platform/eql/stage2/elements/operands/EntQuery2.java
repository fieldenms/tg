package ua.com.fielden.platform.eql.stage2.elements.operands;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class EntQuery2 implements ISingleOperand2 {

    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<? extends AbstractEntity<?>> resultType;
    public final QueryCategory category;

    public EntQuery2(final EntQueryBlocks2 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final QueryCategory category) {
        this.category = category;
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
    }

    @Override
    public Class<? extends AbstractEntity<?>> type() {
        return resultType;
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
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EntQuery2)) {
            return false;
        }
        
        final EntQuery2 other = (EntQuery2) obj;
        
        return Objects.equal(category, other.category) &&
                Objects.equal(resultType, other.resultType) &&
                Objects.equal(sources, other.sources) &&
                Objects.equal(yields, other.yields) &&
                Objects.equal(conditions, other.conditions) &&
                Objects.equal(groups, other.groups) &&
                Objects.equal(orderings, other.orderings);
    }
}
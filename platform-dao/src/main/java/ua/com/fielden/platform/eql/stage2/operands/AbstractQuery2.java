package ua.com.fielden.platform.eql.stage2.operands;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.sources.Sources2;

public abstract class AbstractQuery2 {

    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<?> resultType;

    public AbstractQuery2(final QueryBlocks2 queryBlocks, final Class<?> resultType) {
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(sources.collectProps());
        result.addAll(conditions.collectProps());
        result.addAll(yields.collectProps());
        result.addAll(groups.collectProps());
        result.addAll(orderings.collectProps());
        
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + sources.hashCode();
        result = prime * result + yields.hashCode();
        result = prime * result + orderings.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQuery2)) {
            return false;
        }

        final AbstractQuery2 other = (AbstractQuery2) obj;

        return Objects.equals(resultType, other.resultType) &&
                Objects.equals(sources, other.sources) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings);
    }
}
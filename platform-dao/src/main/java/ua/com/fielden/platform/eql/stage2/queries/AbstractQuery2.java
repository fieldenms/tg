package ua.com.fielden.platform.eql.stage2.queries;

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public abstract class AbstractQuery2 {

    public final IJoinNode2<? extends IJoinNode3> joinRoot;
    public final Conditions2 whereConditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<?> resultType;

    public AbstractQuery2(final QueryComponents2 queryComponents, final Class<?> resultType) {
        this.joinRoot = queryComponents.joinRoot;
        this.whereConditions = queryComponents.whereConditions;
        this.yields = queryComponents.yields;
        this.groups = queryComponents.groups;
        this.orderings = queryComponents.orderings;
        this.resultType = resultType;
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(joinRoot != null ? joinRoot.collectProps() : emptySet());
        result.addAll(whereConditions.collectProps());
        result.addAll(yields.collectProps());
        result.addAll(groups.collectProps());
        result.addAll(orderings.collectProps());
        
        return result;
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        result.addAll(joinRoot != null ? joinRoot.collectEntityTypes() : emptySet());
        result.addAll(whereConditions.collectEntityTypes());
        result.addAll(yields.collectEntityTypes());
        result.addAll(groups.collectEntityTypes());
        result.addAll(orderings.collectEntityTypes());
        
        return result;
    }
    
    protected static TransformationResult2<IJoinNode3> transformNone(final TransformationContext2 context) {
        return new TransformationResult2<IJoinNode3>(null, context);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + whereConditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((joinRoot == null) ? 0 : joinRoot.hashCode());
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
                Objects.equals(joinRoot, other.joinRoot) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(whereConditions, other.whereConditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings);
    }
}
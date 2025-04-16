package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;
import java.util.Optional;

public class SubQuery2 extends AbstractQuery2 implements ISingleOperand2<SubQuery3> {
    public final boolean isRefetchOnly;
    public final PropType type;

    private SubQuery2(
            final Optional<IJoinNode2<? extends IJoinNode3>> maybeJoinRoot,
            final Conditions2 whereConditions,
            final Yields2 yields,
            final GroupBys2 groups,
            final OrderBys2 orderings,
            final PropType type,
            final boolean isRefetchOnly)
    {
        super(maybeJoinRoot, whereConditions, yields, groups, orderings, type.javaType());
        this.isRefetchOnly = isRefetchOnly;
        this.type = type;
    }

    public SubQuery2(final QueryComponents2 queryComponents, final PropType type, final boolean isRefetchOnly) {
        this(queryComponents.maybeJoinRoot(),
             queryComponents.whereConditions(),
             queryComponents.yields(),
             queryComponents.groups(),
             queryComponents.orderings(),
             type,
             isRefetchOnly);
    }

    @Override
    public TransformationResultFromStage2To3<SubQuery3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new SubQuery3(queryComponentsTr.item, type), queryComponentsTr.updatedContext);
    }

    @Override
    public PropType type() {
        return type;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public boolean isNonnullableEntity() {
        return isRefetchOnly;
    }

    @Override
    public SubQuery2 setOrderings(final OrderBys2 orderings) {
        return new SubQuery2(maybeJoinRoot, whereConditions, yields, groups, orderings, type, isRefetchOnly);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + (isRefetchOnly ? 1231 : 1237);
        return prime * result + SubQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof SubQuery2 that
                  && Objects.equals(type, that.type)
                  && (isRefetchOnly == that.isRefetchOnly)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("refetchOnly", isRefetchOnly)
                .add("type", type);
    }

}

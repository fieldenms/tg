package ua.com.fielden.platform.eql.stage1.operands.queries;

import static ua.com.fielden.platform.eql.stage2.etc.Yields2.nullYields;

import ua.com.fielden.platform.eql.stage1.ITransformableToStage2;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SubQueryForExists2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

/**
 * A structure used for representing queries in the EXISTS statement.
 * <p>
 * Its specificity lies in the fact that queries without explicit yields will yield NULL, which is the most suitable value for EXISTS as the actual values are unimportant.
 * What is important is the presence of records in the result.
 *
 */
public class SubQueryForExists1 extends AbstractQuery1 implements ITransformableToStage2<SubQueryForExists2> {

    public SubQueryForExists1(final QueryComponents1 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public SubQueryForExists2 transform(final TransformationContext1 context) {
        final TransformationContext1 localContext = context;

        if (joinRoot == null) {
            return new SubQueryForExists2(transformSourceless(localContext));
        }

        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(localContext);
        final TransformationContext1 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 whereConditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.querySourceInfoProvider, whereConditions.transform(enhancedContext));
        final Yields2 yields2 = yields.getYields().isEmpty() ? nullYields : yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, whereConditions2, yields2, groups2, orderings2);
        return new SubQueryForExists2(queryComponents2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SubQueryForExists1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SubQueryForExists1;
    }
}
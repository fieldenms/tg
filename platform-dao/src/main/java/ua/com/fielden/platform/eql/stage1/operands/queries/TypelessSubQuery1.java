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
import ua.com.fielden.platform.eql.stage2.operands.queries.TypelessSubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class TypelessSubQuery1 extends AbstractQuery1 implements ITransformableToStage2<TypelessSubQuery2> {

    public TypelessSubQuery1(final QueryComponents1 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public TypelessSubQuery2 transform(final TransformationContext1 context) {
        final TransformationContext1 localContext = context;
        
        if (joinRoot == null) {
            return new TypelessSubQuery2(transformSourceless(localContext));
        }
        
        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(localContext);
        final TransformationContext1 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.querySourceInfoProvider, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.getYields().isEmpty() ? nullYields : yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, conditions2, yields2, groups2, orderings2);
        return new TypelessSubQuery2(queryComponents2);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + TypelessSubQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof TypelessSubQuery1;
    }
}
package ua.com.fielden.platform.eql.stage1.operands.queries;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.H_ENTITY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public static final String ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID = "Auto-yield is not possible when the main source of the query doesn't contain ID property.";

    public SubQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public SubQuery2 transform(final TransformationContext1 context) {
        final TransformationContext1 localContext = context;
        
        if (joinRoot == null) {
            final QueryComponents2 qb = transformSourceless(localContext);
            return new SubQuery2(qb, enhance(null, qb.yields), false);
        }

        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(localContext);
        final TransformationContext1 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.domainInfo, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final Yields2 enhancedYields2 = enhanceYields(yields2, joinRoot2);
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, conditions2, enhancedYields2, groups2, orderings2);
        return new SubQuery2(queryComponents2, enhance(resultType, enhancedYields2), isRefetchOnlyQuery());
    }

    private static PropType enhance(final Class<?> resultType, final Yields2 yields) {
        return resultType == null ? 
                yields.getYields().iterator().next().operand.type() // the case of modelAsPrimitive() no ResultType provided 
                :
                    new PropType(resultType, H_ENTITY); // the case of modelAsEntity(..)
    }
    
    private boolean isRefetchOnlyQuery() {
        return conditions.isIdEqualsExtId();
    }
    
    private static Yields2 enhanceYields(final Yields2 yields, final IJoinNode2<? extends IJoinNode3> joinRoot2) {
        if (!yields.getYields().isEmpty()) {
            return yields;
        } else if (joinRoot2.mainSource().querySourceInfo().getProps().containsKey(ID)) {
            return new Yields2(listOf(new Yield2(new Prop2(joinRoot2.mainSource(), listOf(joinRoot2.mainSource().querySourceInfo().getProps().get(ID))), "", false)));
        } else {
            throw new EqlStage1ProcessingException(ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SubQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SubQuery1;
    }
}
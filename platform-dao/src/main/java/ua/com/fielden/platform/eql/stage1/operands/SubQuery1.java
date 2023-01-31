package ua.com.fielden.platform.eql.stage1.operands;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.SubQuery2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public SubQuery1(final QueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public SubQuery2 transform(final TransformationContext1 context) {
        final TransformationContext1 localContext = context;
        
        if (sources == null) {
            final QueryBlocks2 qb = transformSourceless(localContext);
            return new SubQuery2(qb, enhance(null, qb.yields),  qb.yields.getYields().iterator().next().operand.hibType());
        }

        final TransformationResult1<? extends ISources2<?>> sourcesTr = sources.transform(localContext);
        final TransformationContext1 enhancedContext = sourcesTr.updatedContext;
        final ISources2<? extends ISources3> sources2 = sourcesTr.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(sources2.mainSource(), context, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.mainSource());
        final Yields2 enhancedYields2 = enhanceYields(yields2, sources2);
        final QueryBlocks2 entQueryBlocks = new QueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);
        final Object hibType = resultType == null ? enhancedYields2.getYields().iterator().next().operand.hibType() : LongType.INSTANCE;
        return new SubQuery2(entQueryBlocks, enhance(resultType, enhancedYields2), hibType);
    }

    private static Class<?> enhance(final Class<?> resultType, final Yields2 yields) {
        return resultType == null ? yields.getYields().iterator().next().javaType() : resultType;
    }
    
    private static Yields2 enhanceYields(final Yields2 yields, final ISources2<? extends ISources3> sources2) {
        if (yields.getYields().isEmpty()) {
            final ISingleOperand2<?> yieldedOperand = sources2.mainSource().entityInfo().getProps().containsKey(ID)
                    ? new Prop2(sources2.mainSource(), listOf(sources2.mainSource().entityInfo().getProps().get(ID)))
                    : new Value2(0);
            return new Yields2(listOf(new Yield2(yieldedOperand, "", false)));
        }
        return yields;
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
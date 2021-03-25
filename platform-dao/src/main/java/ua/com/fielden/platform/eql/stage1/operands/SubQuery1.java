package ua.com.fielden.platform.eql.stage1.operands;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
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
import ua.com.fielden.platform.eql.stage2.sources.Sources2;

public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public SubQuery1(final QueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public SubQuery2 transform(final TransformationContext context) {
        final TransformationContext localContext = context.produceForCorrelatedSubquery();
        final TransformationResult<Sources2> sourcesTr = sources.transform(localContext);
        final TransformationContext enhancedContext = sourcesTr.updatedContext;
        final Sources2 sources2 = sourcesTr.item;
        final Conditions2 conditions2 = conditions.transform(enhancedContext);
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.main);
        final Yields2 enhancedYields2 = enhanceYields(yields2, sources2);
        final QueryBlocks2 entQueryBlocks = new QueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);

        return new SubQuery2(entQueryBlocks, resultType);
    }

    private Yields2 enhanceYields(final Yields2 yields, final Sources2 sources2) {
        if (yields.getYields().isEmpty()) {
            final ISingleOperand2<?> yieldedOperand = sources2.main.entityInfo().getProps().containsKey(ID)
                    ? new Prop2(sources2.main, listOf(sources2.main.entityInfo().getProps().get(ID)))
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
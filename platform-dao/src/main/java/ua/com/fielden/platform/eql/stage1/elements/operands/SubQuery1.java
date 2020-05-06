package ua.com.fielden.platform.eql.stage1.elements.operands;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SubQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public SubQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public TransformationResult<SubQuery2> transform(final PropsResolutionContext context) {
        final PropsResolutionContext localResolutionContext = context.produceForCorrelatedSubquery();//isSubQuery() ? context.produceForCorrelatedSubquery() : context.produceForUncorrelatedSubquery();
        // .produceForUncorrelatedSubquery() should be used only for cases of synthetic entities (where source query can only be uncorrelated) -- simple queries as source queries are accessible for correlation
        final TransformationResult<Sources2> sourcesTr = sources.transform(localResolutionContext);
        final TransformationResult<Conditions2> conditionsTr = conditions.transform(sourcesTr.updatedContext);
        final TransformationResult<Yields2> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult<GroupBys2> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult<OrderBys2> orderingsTr = orderings.transform(groupsTr.updatedContext);
        final Yields2 enhancedYields = enhanceYields(yieldsTr.item, sourcesTr.item);
        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sourcesTr.item, conditionsTr.item, enhancedYields, groupsTr.item, orderingsTr.item);

        return new TransformationResult<SubQuery2>(new SubQuery2(entQueryBlocks, resultType), context);//orderingsTr.updatedContext.leaveCorrelatedSubquery());
    }

    private Yields2 enhanceYields(final Yields2 yields, final Sources2 sources2) {
        if (yields.getYields().isEmpty()) {
            if (sources2.main.entityInfo().getProps().containsKey(ID)) {
                return new Yields2(listOf(new Yield2(new EntProp2(sources2.main, listOf(sources2.main.entityInfo().getProps().get(ID))), "", false)));
            } else {
                return new Yields2(listOf(new Yield2(new EntValue2(0), "", false)));
            }
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
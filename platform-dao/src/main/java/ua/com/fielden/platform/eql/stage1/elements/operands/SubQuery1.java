package ua.com.fielden.platform.eql.stage1.elements.operands;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
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
import ua.com.fielden.platform.types.tuples.T2;

public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public SubQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public SubQuery2 transform(final PropsResolutionContext context) {
        final PropsResolutionContext localResolutionContext = context.produceForCorrelatedSubquery();
        final T2<Sources2,PropsResolutionContext> sourcesTr = sources.transform(localResolutionContext);
        final PropsResolutionContext enhancedContext = sourcesTr._2; 
        final Sources2 sources2 = sourcesTr._1;
        final Conditions2 conditions2 = conditions.transform(enhancedContext);
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.main);
        final Yields2 enhancedYields2 = enhanceYields(yields2, sources2);
        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);

        return new SubQuery2(entQueryBlocks, resultType);
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
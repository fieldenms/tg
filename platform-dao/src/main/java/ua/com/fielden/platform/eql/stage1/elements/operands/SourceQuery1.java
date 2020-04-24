package ua.com.fielden.platform.eql.stage1.elements.operands;

import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class SourceQuery1 extends AbstractQuery1 implements ITransformableToS2<SourceQuery2> {

    public SourceQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
        assert (resultType != null);
    }

    @Override
    public TransformationResult<SourceQuery2> transform(final PropsResolutionContext context) {
        final PropsResolutionContext localResolutionContext = context.produceForCorrelatedSubquery();//isSubQuery() ? context.produceForCorrelatedSubquery() : context.produceForUncorrelatedSubquery();
        // .produceForUncorrelatedSubquery() should be used only for cases of synthetic entities (where source query can only be uncorrelated) -- simple queries as source queries are accessible for correlation
        final TransformationResult<Sources2> sourcesTr = sources != null ? sources.transform(localResolutionContext) : null;
        final TransformationResult<Conditions2> conditionsTr = conditions.transform(sourcesTr != null ? sourcesTr.updatedContext : localResolutionContext);
        final TransformationResult<Yields2> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult<GroupBys2> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult<OrderBys2> orderingsTr = orderings.transform(groupsTr.updatedContext);
        final Yields2 enhancedYields = sources != null ? enhanceYields(yieldsTr.item, sourcesTr.item) : yieldsTr.item;
        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sourcesTr != null ? sourcesTr.item : null, conditionsTr.item, enhancedYields/*yieldsTr.item*/, groupsTr.item, orderingsTr.item);

        final PropsResolutionContext resultResolutionContext = new PropsResolutionContext(orderingsTr.updatedContext.getDomainInfo(), orderingsTr.updatedContext.getSources().subList(1, orderingsTr.updatedContext.getSources().size()), orderingsTr.updatedContext.sourceId);
        return new TransformationResult<SourceQuery2>(new SourceQuery2(entQueryBlocks, resultType), resultResolutionContext);
    }

    private Yields2 enhanceYields(final Yields2 yields, final Sources2 sources2) {
        if (yields.getYields().isEmpty()) {
            final List<Yield2> enhancedYields = new ArrayList<>();
            for (final Entry<String, AbstractPropInfo<?>> el : sources2.main.entityInfo().getProps().entrySet()) {
                if (!el.getValue().hasExpression()) {
                    enhancedYields.add(new Yield2(new EntProp2(sources2.main, listOf(el.getValue())), el.getKey(), false));
                }
            }
            return new Yields2(enhancedYields);
        }
        return yields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SourceQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SourceQuery1;
    }
}
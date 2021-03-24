package ua.com.fielden.platform.eql.stage1.operands;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.core.GroupBys2;
import ua.com.fielden.platform.eql.stage2.core.OrderBys2;
import ua.com.fielden.platform.eql.stage2.core.Yield2;
import ua.com.fielden.platform.eql.stage2.core.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.sources.QrySources2;
import ua.com.fielden.platform.eql.stage3.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class SourceQuery1 extends AbstractQuery1 implements ITransformableToS2<SourceQuery2> {
    
    /**
     * All simple queries as source queries are accessible for correlation. Source queries derived from synthetic entities can't be correlated.
     */
    public final boolean isCorrelated;
    
    public SourceQuery1(final QueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final boolean isCorrelated) {
        super(queryBlocks, requireNonNull(resultType));
        this.isCorrelated = isCorrelated;
    }

    @Override
    public SourceQuery2 transform(final PropsResolutionContext context) {
        final PropsResolutionContext localResolutionContext = isCorrelated ? context.produceForCorrelatedSourceQuery() : context.produceForUncorrelatedSourceQuery();
        final T2<QrySources2,PropsResolutionContext> sourcesTr = sources.transform(localResolutionContext);
        final PropsResolutionContext enhancedContext = sourcesTr._2; 
        final QrySources2 sources2 = sourcesTr._1;
        final Conditions2 conditions2 = conditions.transform(enhancedContext);
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.main);
        final Yields2 enhancedYields2 = expand(enhanceYields(yields2, sources2.main));
        final QueryBlocks2 entQueryBlocks = new QueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);
        return new SourceQuery2(entQueryBlocks, resultType);
    }

    private Yields2 enhanceYields(final Yields2 yields, final IQrySource2<? extends IQrySource3> mainSource) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());
            for (final Entry<String, AbstractPropInfo<?>> el : mainSource.entityInfo().getProps().entrySet()) {
                if (!el.getValue().hasExpression()) {
                    enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(el.getValue())), el.getKey(), false));
                }
            }
            return new Yields2(enhancedYields, yields.getYields().isEmpty());
        }
        
        final Yield2 firstYield = yields.getYields().iterator().next();
        if (yields.getYields().size() == 1 && !yieldAll && isEmpty(firstYield.alias) && isPersistedEntityType(resultType)) {
            return new Yields2(listOf(new Yield2(firstYield.operand, ID, firstYield.hasRequiredHint)));
        }
        
        return yields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isCorrelated ? 1231 : 1237);
        return prime * result + SourceQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SourceQuery1 && ((SourceQuery1) obj).isCorrelated == isCorrelated;
    }
}
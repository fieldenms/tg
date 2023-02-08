package ua.com.fielden.platform.eql.stage1.operands.queries;

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
import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class SourceQuery1 extends AbstractQuery1 implements ITransformableToS2<SourceQuery2> {

    /**
     * All simple queries as source queries are accessible for correlation. Source queries derived from synthetic entities can't be correlated.
     */
    public final boolean isCorrelated;

    public SourceQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType, final boolean isCorrelated) {
        super(queryComponents, requireNonNull(resultType));
        this.isCorrelated = isCorrelated;
    }

    @Override
    public SourceQuery2 transform(final TransformationContext1 context) {
        final TransformationContext1 localContext = isCorrelated ? context : new TransformationContext1(context.domainInfo);

        if (joinRoot == null) {
            return new SourceQuery2(transformSourceless(localContext), resultType);
        }

        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(localContext);
        final TransformationContext1 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.domainInfo, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final Yields2 enhancedYields2 = expand(enhanceYields(yields2, joinRoot2.mainSource(), context.shouldIncludeCalcProps));
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, conditions2, enhancedYields2, groups2, orderings2);
        return new SourceQuery2(queryComponents2, resultType);
    }

    private Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource, final boolean shouldIncludeCalcProps) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());
            for (final Entry<String, AbstractPropInfo<?>> el : mainSource.entityInfo().getProps().entrySet()) {
                if (!el.getValue().hasExpression() || shouldIncludeCalcProps && !(el.getValue().hasAggregation() || el.getValue().implicit)) {
                    enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(el.getValue())), el.getKey(), false));
                }
            }
            return new Yields2(enhancedYields, yields.getYields().isEmpty() && !shouldIncludeCalcProps);
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
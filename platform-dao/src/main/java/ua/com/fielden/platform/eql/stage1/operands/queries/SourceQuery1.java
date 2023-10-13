package ua.com.fielden.platform.eql.stage1.operands.queries;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.ITransformableToStage2;
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
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * A structure used for representing queries in the FROM/JOIN statements.
 * Technically these queries are sub queries from SQL point of view, but the nomenclature of the Sub-Query is reserved for sub queries in other parts (i.e., not FROM/JOIN).
 * <p>
 * The specificity of this structure pertains to the fact that in case of no explicit yields or {@code yieldAll}, yields are derived from the main source of the query. No fetch models affect this.
 */
public class SourceQuery1 extends AbstractQuery1 implements ITransformableToStage2<SourceQuery2> {

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
        final TransformationContext1 localContext = isCorrelated ? context : new TransformationContext1(context.querySourceInfoProvider, context.isForCalcProp);

        if (joinRoot == null) {
            return new SourceQuery2(transformSourceless(localContext), resultType);
        }

        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(localContext);
        final TransformationContext1 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 whereConditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.querySourceInfoProvider, whereConditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final Yields2 enhancedYields2 = enhanceYields(yields2, joinRoot2.mainSource(), context.shouldMaterialiseCalcPropsAsColumnsInSqlQuery);
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, whereConditions2, enhancedYields2, groups2, orderings2);
        return new SourceQuery2(queryComponents2, resultType);
    }

    private Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource, final boolean shouldIncludeCalcProps) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());
            for (final Entry<String, AbstractQuerySourceItem<?>> el : mainSource.querySourceInfo().getProps().entrySet()) {
                if (!el.getValue().hasExpression() || shouldIncludeCalcProps && !(el.getValue().hasAggregation() || el.getValue().implicit)) {

                    if (el.getValue() instanceof QuerySourceItemForUnionType) {
                        for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForUnionType<?>) el.getValue()).getProps().entrySet()) {
                            if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(el.getValue(), sub.getValue())), el.getKey() + "." + sub.getValue().name, false));
                            }
                        }
                    } else if (el.getValue() instanceof QuerySourceItemForComponentType) {
                        for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForComponentType<?>) el.getValue()).getSubitems().entrySet()) {
                            enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(el.getValue(), sub.getValue())), el.getKey() + "." + sub.getValue().name, false));
                        }
                    } else {
                        enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(el.getValue())), el.getKey(), false));
                    }
                }
            }
            final boolean allGenerated = mainSource.querySourceInfo().isComprehensive && yields.getYields().isEmpty() && !shouldIncludeCalcProps;
            // generated yields with shouldIncludeCalcProps=true will produce different QuerySourceInfo from the canonical one (calc props will be yielded, thus turned from calc to persistent)
            // if necessary additional separate cache can be created for such cases (allGeneratedButWithCalcPropsMaterialised)
            return new Yields2(enhancedYields, allGenerated);
        }

        final Yield2 firstYield = yields.getYields().iterator().next();
        if (yields.getYields().size() == 1 && !yieldAll && isEmpty(firstYield.alias) && isPersistedEntityType(resultType)) {
            return new Yields2(listOf(new Yield2(firstYield.operand, ID, firstYield.hasNonnullableHint)));
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
package ua.com.fielden.platform.eql.stage1.operands.queries;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceInfoItem;
import ua.com.fielden.platform.eql.meta.query.ComponentTypeQuerySourceInfoItem;
import ua.com.fielden.platform.eql.meta.query.EntityTypeQuerySourceInfoItem;
import ua.com.fielden.platform.eql.meta.query.UnionTypeQuerySourceInfoItem;
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
import ua.com.fielden.platform.eql.stage2.operands.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.types.tuples.T2;

public class ResultQuery1 extends AbstractQuery1 implements ITransformableToS2<ResultQuery2> {

    public final IRetrievalModel<?> fetchModel;

    public ResultQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType, final IRetrievalModel<?> fetchModel) {
        super(queryComponents, requireNonNull(resultType));
        this.fetchModel = fetchModel;
    }

    @Override
    public ResultQuery2 transform(final TransformationContext1 context) {
        if (joinRoot == null) {
            return new ResultQuery2(transformSourceless(context), resultType);
        }
        final T2<TransformationResult1<? extends IJoinNode2<?>>, Boolean> joinRootTr = transformAndEnhanceJoinRoot(context);
        final TransformationContext1 enhancedContext = joinRootTr._1.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr._1.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context.querySourceInfoProvider, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        final Yields2 enhancedYields2 = enhanceYields(yields2, joinRoot2.mainSource(), joinRootTr._2);
        final QueryComponents2 queryComponents2 = new QueryComponents2(joinRoot2, conditions2, enhancedYields2, groups2, orderings2);

        return new ResultQuery2(queryComponents2, resultType);
    }
    
    private T2<TransformationResult1<? extends IJoinNode2<?>>, Boolean> transformAndEnhanceJoinRoot(final TransformationContext1 context) {
        final TransformationResult1<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(context);
        if (fetchModel == null) {
            return T2.t2(joinRootTr, false);
        }
        
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        boolean allAggregated = false;
        final ISource2<? extends ISource3> mainSource = joinRoot2.mainSource();
        if (mainSource.sourceType().equals(fetchModel.getEntityType())) {
            allAggregated = true;
            for (final String primProp : fetchModel.getPrimProps()) {
                final AbstractQuerySourceInfoItem<?> fetchedProp = mainSource.querySourceInfo().getProps().get(primProp.split("\\.")[0]);
                if (fetchedProp != null) {
                    allAggregated = allAggregated && fetchedProp.hasAggregation();
                }
            }
        }
        
        if (!allAggregated) {
            return T2.t2(joinRootTr, false);
        } else {
            // Need to repeat the transformation of joinRoot in order to actually yield all calculated properties. This way they will look as usual properties (contain no aggregation) at query source usage level.
            return T2.t2(joinRoot.transform(context.cloneForAggregates()), true);
        }
    }
    
    /**
     * Enhances {@code yields}, which were determined during EQL stage2 processing, with additional yields:
     * <ol> 
     * <li> No yields or {@code yieldAll} - adds all properties that belong to {@code mainSource} and are also present in {@code fetchModel}; in case of entity-typed properties, their properties are also included (if they exist in a fetch model) to improve query performance.
     *      It is important to note that in case of the synthetic entities (excluding the case of fetching totals only), {@code id} is added to the yields.
     *      This is necessary to overcome the current limitation of fetch strategies that ignore {@code id} for synthetic entities. 
     * <li> Single yield {@code .modelAsEntity} - enhances the yield with "id" as alias. 
     * </ol>
     * 
     * @param yields
     * @param mainSource
     * @param allAggregated
     * @return
     */
    private Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource, final boolean allAggregated) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());

            final boolean isNotTopFetch = fetchModel == null ? false : !fetchModel.topLevel();
            
            for (final Entry<String, AbstractQuerySourceInfoItem<?>> l1Prop : mainSource.querySourceInfo().getProps().entrySet()) {
            	// FIXME condition for {@code id} should be removed once the default fetch strategies are adjusted to recognise the presence of {@code id} in synthetic entities.
                if (fetchModel == null || fetchModel.containsProp(l1Prop.getValue().name) || (!allAggregated && ID.equals(l1Prop.getValue().name) && isSyntheticEntityType(resultType))) {
                    final EntityRetrievalModel<? extends AbstractEntity<?>> l1PropFm = fetchModel == null ? null : fetchModel.getRetrievalModels().get(l1Prop.getValue().name);
                    final boolean yieldSubprops = isNotTopFetch && l1PropFm != null && l1Prop.getValue() instanceof EntityTypeQuerySourceInfoItem;
                    
                    if (!yieldSubprops) {
                        if (l1Prop.getValue() instanceof UnionTypeQuerySourceInfoItem) {
                            for (final Entry<String, AbstractQuerySourceInfoItem<?>> sub : ((UnionTypeQuerySourceInfoItem<?>) l1Prop.getValue()).getProps().entrySet()) {
                                if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                    enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + sub.getValue().name, false));
                                }
                            }
                        } else if (l1Prop.getValue() instanceof ComponentTypeQuerySourceInfoItem) {
                            for (final Entry<String, AbstractQuerySourceInfoItem<?>> sub : ((ComponentTypeQuerySourceInfoItem<?>) l1Prop.getValue()).getSubitems().entrySet()) {
                                enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + sub.getValue().name, false));
                            }
                        } else {
                            enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue())), l1Prop.getKey(), false));    
                        }
                    } else {
                        final EntityTypeQuerySourceInfoItem<?> l1PropMd = ((EntityTypeQuerySourceInfoItem<?>) l1Prop.getValue());
                        for (final Entry<String, AbstractQuerySourceInfoItem<?>> l2Prop : l1PropMd.querySourceInfo.getProps().entrySet()) {
                            if (l1PropFm.containsProp(l2Prop.getValue().name)) {
                                if (l2Prop.getValue() instanceof UnionTypeQuerySourceInfoItem) {
                                    for (final Entry<String, AbstractQuerySourceInfoItem<?>> sub : ((UnionTypeQuerySourceInfoItem<?>) l2Prop.getValue()).getProps().entrySet()) {
                                        if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                            enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), l2Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + l2Prop.getKey() + "." + sub.getValue().name, false));
                                        }
                                    }
                                } else if (l2Prop.getValue() instanceof ComponentTypeQuerySourceInfoItem) {
                                    for (final Entry<String, AbstractQuerySourceInfoItem<?>> sub : ((ComponentTypeQuerySourceInfoItem<?>) l2Prop.getValue()).getSubitems().entrySet()) {
                                        enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), l2Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + l2Prop.getKey() + "." + sub.getValue().name, false));
                                    }
                                } else {
                                    enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), l2Prop.getValue())), l1Prop.getKey() + "." + l2Prop.getKey(), false));
                                }
                            }
                        }
                    }
                }
            }
            
            return new Yields2(enhancedYields);
        }

        final Yield2 firstYield = yields.getYields().iterator().next();
        if (yields.getYields().size() == 1 && !yieldAll && isEmpty(firstYield.alias) && isPersistedEntityType(resultType)) {
            return new Yields2(listOf(new Yield2(firstYield.operand, ID, firstYield.hasNonnullableHint)));
        }

        //TODO need to remove the yields not contained by the fetch model to be consisted with old EQL (the case of explicit yields)
        
        return yields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fetchModel == null) ? 0 : fetchModel.hashCode());
        return prime * result + ResultQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery1 && Objects.equal(fetchModel, ((ResultQuery1) obj).fetchModel);
    }
}

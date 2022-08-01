package ua.com.fielden.platform.eql.stage1.operands;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class ResultQuery1 extends AbstractQuery1 implements ITransformableToS2<ResultQuery2> {

    public final IRetrievalModel<?> fetchModel;

    public ResultQuery1(final QueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final IRetrievalModel<?> fetchModel) {
        super(queryBlocks, requireNonNull(resultType));
        this.fetchModel = fetchModel;
    }

    @Override
    public ResultQuery2 transform(final TransformationContext context) {
        if (sources == null) {
            return new ResultQuery2(transformSourceless(context), resultType);
        }
        final TransformationResult<? extends ISources2<?>> sourcesTr = transformAndEnhanceSource(context);
        final TransformationContext enhancedContext = sourcesTr.updatedContext;
        final ISources2<? extends ISources3> sources2 = sourcesTr.item;
        final Conditions2 conditions2 = enhanceWithUserDataFilterConditions(sources2.mainSource(), context, conditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.mainSource());
        final Yields2 enhancedYields2 = expand(enhanceYields(yields2, sources2.mainSource()));
        final QueryBlocks2 entQueryBlocks = new QueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);

        return new ResultQuery2(entQueryBlocks, resultType);
    }
    
    private TransformationResult<? extends ISources2<?>> transformAndEnhanceSource(final TransformationContext context) {
        final TransformationResult<? extends ISources2<?>> sourcesTr = sources.transform(context);
        if (fetchModel == null) {
            return sourcesTr;
        }
        
        final ISources2<? extends ISources3> sources2 = sourcesTr.item;
        boolean allAggregated = false;
        final ISource2<? extends ISource3> mainSource = sources2.mainSource();
        if (mainSource.sourceType().equals(fetchModel.getEntityType())) {
            allAggregated = true;
            for (final String primProp : fetchModel.getPrimProps()) {
                final AbstractPropInfo<?> fetchedProp = mainSource.entityInfo().getProps().get(primProp.split("\\.")[0]);
                if (fetchedProp != null) {
                    allAggregated = allAggregated && fetchedProp.hasAggregation();
                }
            }
        }
        
        if (!allAggregated) {
            return sourcesTr;
        } else {
            return new TransformationResult<>(sourcesTr.item, sourcesTr.updatedContext.cloneForAggregates());
        } 
    }
    
    private Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());

            final boolean isNotTopFetch = fetchModel == null ? false : !fetchModel.topLevel();
            for (final Entry<String, AbstractPropInfo<?>> l1Prop : mainSource.entityInfo().getProps().entrySet()) {
                if (fetchModel == null || fetchModel.containsProp(l1Prop.getValue().name)) {
                    final EntityRetrievalModel<? extends AbstractEntity<?>> l1PropFm = fetchModel == null ? null : fetchModel.getRetrievalModels().get(l1Prop.getValue().name);
                    final boolean yieldSubprops = isNotTopFetch && l1PropFm != null && l1Prop.getValue() instanceof EntityTypePropInfo;
                    
                    if (!yieldSubprops) {
                        enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue())), l1Prop.getKey(), false));
                    } else {
                        final EntityTypePropInfo<?> l1PropMd = ((EntityTypePropInfo<?>) l1Prop.getValue());
                        for (final Entry<String, AbstractPropInfo<?>> l2Prop : l1PropMd.propEntityInfo.getProps().entrySet()) {
                            if (l1PropFm.containsProp(l2Prop.getValue().name)) {
                                enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), l2Prop.getValue())), l1Prop.getKey() + "." + l2Prop.getKey(), false));
                            }
                        }
                    }
                }
            }
            return new Yields2(enhancedYields);
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
        result = prime * result + ((fetchModel == null) ? 0 : fetchModel.hashCode());
        return prime * result + ResultQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery1 && Objects.equal(fetchModel, ((ResultQuery1) obj).fetchModel);
    }
}

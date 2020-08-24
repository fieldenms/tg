package ua.com.fielden.platform.eql.stage1.elements.operands;

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
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class ResultQuery1 extends AbstractQuery1 implements ITransformableToS2<ResultQuery2> {
    
    public final IRetrievalModel<?> fetchModel;

    public ResultQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final IRetrievalModel<?> fetchModel) {
        super(queryBlocks, requireNonNull(resultType));
        this.fetchModel = fetchModel;
    }

    @Override
    public ResultQuery2 transform(final PropsResolutionContext context) {
        final T2<Sources2, PropsResolutionContext> sourcesTr = sources.transform(context);
        final PropsResolutionContext enhancedContext = sourcesTr._2;
        final Sources2 sources2 = sourcesTr._1;
        final Conditions2 conditions2 = conditions.transform(enhancedContext);
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.main);
        final Yields2 enhancedYields2 = enhanceYields(yields2, sources2.main);
        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(sources2, conditions2, enhancedYields2, groups2, orderings2);

        return new ResultQuery2(entQueryBlocks, resultType);
    }

    private Yields2 enhanceYields(final Yields2 yields, final IQrySource2<? extends IQrySource3> mainSource) {
        // TODO include all props that are available (including calc-props) and also present in fetch model.

        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());
            for (final Entry<String, AbstractPropInfo<?>> el : mainSource.entityInfo().getProps().entrySet()) {
                if (fetchModel.containsProp(el.getValue().name)) {
                    enhancedYields.add(new Yield2(new EntProp2(mainSource, listOf(el.getValue())), el.getKey(), false, (el.getValue() instanceof UnionTypePropInfo || el.getValue() instanceof ComponentTypePropInfo))); 
                    
                    if (el.getValue() instanceof UnionTypePropInfo) {
                        for (final Entry<String, AbstractPropInfo<?>> sub : ((UnionTypePropInfo<?>) el.getValue()).propEntityInfo.getProps().entrySet()) {
                            if (EntityUtils.isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                enhancedYields.add(new Yield2(new EntProp2(mainSource, listOf(el.getValue(), sub.getValue())), el.getKey() + "." + sub.getKey(), false));             
                            }
                        }
                    } else if (el.getValue() instanceof ComponentTypePropInfo) {
                        for (final Entry<String, AbstractPropInfo<?>> sub : ((ComponentTypePropInfo<?>) el.getValue()).getProps().entrySet()) {
                            enhancedYields.add(new Yield2(new EntProp2(mainSource, listOf(el.getValue(), sub.getValue())), el.getKey() + "." + sub.getKey(), false));             
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
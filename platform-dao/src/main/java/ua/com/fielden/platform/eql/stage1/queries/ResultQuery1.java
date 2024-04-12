package ua.com.fielden.platform.eql.stage1.queries;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
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
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * A structure used for representing the most outer query that is used to actually execute to get some data out.
 * <p>
 * Yield processing is subject to the following rules:
 * <ul>
 *   <li> In case of no explicit yields or {@code yieldAll}, fetch models are used for auto-yielding.
 *   <li> In case of a single unaliased yield when the query result is an entity type, alias ID is used.
 * </ul>
 *
 * @author TG Team
 */
public class ResultQuery1 extends AbstractQuery1 implements ITransformableFromStage1To2<ResultQuery2> {

    public final IRetrievalModel<?> fetchModel;

    public ResultQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType, final IRetrievalModel<?> fetchModel) {
        super(queryComponents, requireNonNull(resultType));
        this.fetchModel = fetchModel;
    }

    @Override
    public ResultQuery2 transform(final TransformationContextFromStage1To2 context) {
        return new ResultQuery2(joinRoot == null ? transformSourceless(context) : transformQueryComponents(context), resultType);
    }

    /**
     * Enhances {@code yields}, which were determined during EQL stage2 processing, with additional yields:
     * <ol>
     * <li> No yields or {@code yieldAll} - adds all properties that belong to {@code mainSource} and are also present in {@code fetchModel}; in case of entity-typed properties and being one of the queries, constructed during fetching process (i.e., not the main user query), their properties are also included (if they exist in a fetch model) to improve query performance.
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
    @Override
    protected Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYields().isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());

            final boolean isNotTopFetch = fetchModel == null ? false : !fetchModel.topLevel();

            for (final Entry<String, AbstractQuerySourceItem<?>> l1Prop : mainSource.querySourceInfo().getProps().entrySet()) {
            	// FIXME condition for {@code id} should be removed once the default fetch strategies are adjusted to recognise the presence of {@code id} in synthetic entities.
                if (fetchModel == null || fetchModel.containsProp(l1Prop.getValue().name) || (!fetchModel.containsOnlyTotals() && ID.equals(l1Prop.getValue().name) && isSyntheticEntityType(resultType))) {
                    final EntityRetrievalModel<? extends AbstractEntity<?>> l1PropFm = fetchModel == null ? null : fetchModel.getRetrievalModels().get(l1Prop.getValue().name);
                    final boolean yieldSubprops = isNotTopFetch && l1PropFm != null && l1Prop.getValue() instanceof QuerySourceItemForEntityType;

                    if (!yieldSubprops) {
                        if (l1Prop.getValue() instanceof QuerySourceItemForUnionType) {
                            for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForUnionType<?>) l1Prop.getValue()).getProps().entrySet()) {
                                if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                    enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + sub.getValue().name, false));
                                }
                            }
                        } else if (l1Prop.getValue() instanceof QuerySourceItemForComponentType) {
                            for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForComponentType<?>) l1Prop.getValue()).getSubitems().entrySet()) {
                                enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + sub.getValue().name, false));
                            }
                        } else {
                            enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue())), l1Prop.getKey(), false));
                        }
                    } else {
                        final QuerySourceItemForEntityType<?> l1PropMd = ((QuerySourceItemForEntityType<?>) l1Prop.getValue());
                        for (final Entry<String, AbstractQuerySourceItem<?>> l2Prop : l1PropMd.querySourceInfo.getProps().entrySet()) {
                            if (l1PropFm.containsProp(l2Prop.getValue().name)) {
                                if (l2Prop.getValue() instanceof QuerySourceItemForUnionType) {
                                    for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForUnionType<?>) l2Prop.getValue()).getProps().entrySet()) {
                                        if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                                            enhancedYields.add(new Yield2(new Prop2(mainSource, listOf(l1Prop.getValue(), l2Prop.getValue(), sub.getValue())), l1Prop.getKey() + "." + l2Prop.getKey() + "." + sub.getValue().name, false));
                                        }
                                    }
                                } else if (l2Prop.getValue() instanceof QuerySourceItemForComponentType) {
                                    for (final Entry<String, AbstractQuerySourceItem<?>> sub : ((QuerySourceItemForComponentType<?>) l2Prop.getValue()).getSubitems().entrySet()) {
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

        // TODO need to remove the explicit yields, not contained in the fetch model to be consistent with EQL2.
        // This more of a desire to guarantee that columns in the SELECT statement are not wider than the fetch model specifies.

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

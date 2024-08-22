package ua.com.fielden.platform.eql.stage1.queries;

import com.google.common.base.Objects;
import ua.com.fielden.platform.entity.AbstractEntity;
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
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.*;

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
     * <li> No yields or {@code yieldAll} - adds all properties that belong to {@code mainSource} and are also present in {@code fetchModel}.
     *   <ul>
     *   <li> In case of entity-typed properties and being one of the queries constructed during fetching process (i.e., not the main user query),
     *        their properties are also included (if they exist in the fetch model) to improve query performance.
     *   <li> In case of synthetic entities (excluding the case of fetching totals only), {@code id} is also yielded.
     *        This is necessary to overcome the current limitation of fetch strategies that ignore {@code id} for synthetic entities.
     *   </ul>
     * <li> Single yield {@code .modelAsEntity()} - enhances that yield with {@code "id"} as alias.
     * </ol>
     */
    @Override
    protected Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        return first(yields.getYields())
                .filter($ -> !yieldAll)
                .map(fstYield -> enhanceNonEmptyAndNotYieldAll(fstYield, yields, mainSource))
                .orElseGet(() -> enhanceAll(mainSource));
    }

    private Yields2 enhanceNonEmptyAndNotYieldAll(final Yield2 fstYield, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYields().size() == 1 && isEmpty(fstYield.alias) && isPersistedEntityType(resultType)) {
            return new Yields2(List.of(new Yield2(fstYield.operand, ID, fstYield.hasNonnullableHint)));
        }

        // TODO need to remove the explicit yields, not contained in the fetch model to be consistent with EQL2.
        // This more of a desire to guarantee that columns in the SELECT statement are not wider than the fetch model specifies.

        return yields;
    }

    private Yields2 enhanceAll(final ISource2<? extends ISource3> mainSource) {
        final boolean isNotTopFetch = fetchModel != null && !fetchModel.topLevel();
        final boolean fetchOnlyTotals = fetchModel != null && fetchModel.containsOnlyTotals();
        final boolean synResulType = isSyntheticEntityType(resultType);
        final var enhancedYields = mainSource.querySourceInfo().getProps().values().stream()
                // FIXME condition for {@code id} should be removed once default fetch strategies are adjusted
                // to recognise presence of {@code id} in synthetic entities.
                .filter(prop1 -> fetchModel == null || fetchModel.containsProp(prop1.name)
                                 || (!fetchOnlyTotals && ID.equals(prop1.name) && synResulType))
                // prop -> stream of prop path components
                .flatMap(prop1 -> {
                    final var prop1FetchModel = fetchModel == null ? null : fetchModel.getRetrievalModels().get(prop1.name);
                    if (isNotTopFetch && prop1FetchModel != null && prop1 instanceof QuerySourceItemForEntityType<?> entityProp1) {
                        // yielding subproperties
                        return entityProp1.querySourceInfo.getProps().values().stream()
                                .filter(prop2 -> prop1FetchModel.containsProp(prop2.name))
                                .flatMap(prop2 -> streamSubProps(prop2)
                                        .map(optProp3 -> Stream.concat(Stream.of(prop1, prop2), optProp3.stream())));
                    } else {
                        return streamSubProps(prop1).map(optProp2 -> StreamUtils.prepend(prop1, optProp2.stream()));
                    }
                })
                .map(Stream::toList)
                .map(props -> new Yield2(new Prop2(mainSource, props),
                                         props.stream().map(i -> i.name).collect(joining(".")),
                                         false))
                .toList();

        return new Yields2(enhancedYields);
    }

    private Stream<Optional<AbstractQuerySourceItem<?>>> streamSubProps(AbstractQuerySourceItem<?> prop1) {
        return switch (prop1) {
            case QuerySourceItemForUnionType<?> unionProp1 ->
                    unionProp1.getProps().values().stream()
                            .filter(unionProp2 -> isEntityType(unionProp2.javaType()) && !unionProp2.hasExpression())
                            .map(Optional::of);
            case QuerySourceItemForComponentType<?> componentProp1 ->
                    componentProp1.getSubitems().values().stream().map(Optional::of);
            default -> Stream.of(Optional.empty());
        };
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

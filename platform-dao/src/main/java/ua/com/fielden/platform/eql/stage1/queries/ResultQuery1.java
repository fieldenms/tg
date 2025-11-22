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
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

/**
 * Represents a top-level query that produces results.
 * <h3> Transformation to stage 2 </h3>
 * Processing of yields is subject to the following rules:
 * <ul>
 *   <li> In case of no explicit yields or "yield all", the query source is used to expand the yields, which are then adjusted according to the fetch model.
 *        Yields are expanded by taking each property from the query source and yielding it as if {@code yield().prop("x").as("x")} was used.
 *   <li> In case of a single unaliased yield when the query result is a persistent entity type, alias {@code id} is used.
 * </ul>
 *
 * @author TG Team
 */
public class ResultQuery1 extends AbstractQuery1 implements ITransformableFromStage1To2<ResultQuery2> {

    public static final String ERR_NO_YIELDS = "Nothing can be yielded. Either the query source is empty, or yields were discarded by the fetch model.\n";

    public final IRetrievalModel<?> retrievalModel;

    public ResultQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType, final IRetrievalModel<?> retrievalModel) {
        super(queryComponents, requireNonNull(resultType));
        this.retrievalModel = retrievalModel;
    }

    @Override
    public ResultQuery2 transform(final TransformationContextFromStage1To2 context) {
        final var queryComponents = maybeJoinRoot.map(joinRoot -> transformQueryComponents(context, joinRoot))
                .orElseGet(() -> transformSourceless(context));
        return new ResultQuery2(queryComponents, resultType);
    }

    /**
     * Enhances yields according to the following rules:
     * <ol>
     * <li> No yields or {@code yieldAll} - adds all properties that belong to {@code mainSource} and are also present in the fetch model.
     *   <ul>
     *   <li> In the case of entity-typed properties and being one of the queries constructed during the fetching process (i.e., not the main user query),
     *        their properties are also included (if they exist in the fetch model) to improve query performance.
     *   <li> In case of synthetic entities (excluding the case of fetching totals only), {@code id} is also yielded.
     *        This is necessary to overcome the current limitation of fetch strategies that ignore {@code id} for synthetic entities.
     *   </ul>
     * <li> A single unalised yield with the result type being a persistent entity type - enhances that yield with {@code "id"} as alias.
     * <li> Otherwise, no enhancements are performed.
     * </ol>
     */
    @Override
    protected EnhancedYields enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        return finaliseYields(yields,
                              first(yields.getYields())
                                      .filter($ -> !yieldAll)
                                      .map(fstYield -> enhanceNonEmptyAndNotYieldAll(fstYield, yields, mainSource))
                                      .orElseGet(() -> enhanceAll(mainSource)));
    }

    private EnhancedYields enhanceNonEmptyAndNotYieldAll(final Yield2 fstYield, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYields().size() == 1 && isEmpty(fstYield.alias()) && isPersistentEntityType(resultType)) {
            return new EnhancedYields(new Yields2(List.of(new Yield2(fstYield.operand(), ID, fstYield.hasNonnullableHint()))));
        }

        // TODO: Need to remove the explicit yields, not contained in the fetch model to be consistent with the approach used in EQL2.
        //       This is more of a desire to guarantee that columns in the SELECT statement are not wider than the fetch model specifies.

        return new EnhancedYields(yields);
    }

    private EnhancedYields enhanceAll(final ISource2<? extends ISource3> mainSource) {
        final boolean isNotTopFetch = retrievalModel != null && !retrievalModel.isTopLevel();
        final var enhancedYields = mainSource.querySourceInfo().getProps().values().stream()
                // Narrow down the set of yields to those included in the fetch model.
                // NOTE: If `retrievalModel` is null and the result type is a union entity, this code may add yields for common properties, which will fail during instantiation.
                //       However, this is unlikely to occur in practice.
                //       `retrievalModel` may be null only in low-level platform code that should know what it is doing.
                .filter(level1Prop -> retrievalModel == null || retrievalModel.containsProp(level1Prop.name))
                // prop -> stream of prop path components
                .flatMap(level1Prop -> {
                    final var level1PropFetchModel = retrievalModel == null ? null : retrievalModel.getRetrievalModelOpt(level1Prop.name).orElse(null);
                    if (isNotTopFetch && level1PropFetchModel != null && level1Prop instanceof QuerySourceItemForEntityType<?> level1EntityProp) {
                        // yielding sub-properties
                        return level1EntityProp.querySourceInfo.getProps().values().stream()
                                .filter(level2Prop -> level1PropFetchModel.containsProp(level2Prop.name))
                                .flatMap(level2Prop -> streamSubProps(level2Prop)
                                                       .map(optLevel3Prop -> Stream.concat(Stream.of(level1Prop, level2Prop), optLevel3Prop.stream())));
                    } else {
                        return streamSubProps(level1Prop).map(optProp2 -> StreamUtils.prepend(level1Prop, optProp2.stream()));
                    }
                })
                .map(Stream::toList)
                .map(props -> new Yield2(new Prop2(mainSource, props),
                                         props.stream().map(i -> i.name).collect(joining(".")),
                                         false))
                .toList();

        return new EnhancedYields(new Yields2(enhancedYields)) {
           @Override protected String formatErrorEmptyYields() {
               return ToString.separateLines()
                       .toString(ERR_NO_YIELDS)
                       .addIfNotNull("Retrieval model", retrievalModel)
                       .add("Source", mainSource.toStringCompact())
                       .$();
           }
       };
    }

    /**
     * A helper function to stream sub-properties of {@code prop}, if any.
     *
     * @param prop a property source
     * @return a stream of optional sub-properties of {@code prop}; the result can stream empty optionals.
     */
    // TODO: More than 1 empty optional in the stream can lead to unexpected results.
    //       Consider changing the return type to Optional<Stream>.
    private static Stream<Optional<AbstractQuerySourceItem<?>>> streamSubProps(final AbstractQuerySourceItem<?> prop) {
        return switch (prop) {
            case QuerySourceItemForUnionType<?> unionTypedProp ->
                    unionTypedProp.getProps().values().stream()
                            .filter(unionProp -> isEntityType(unionProp.javaType()) && !unionProp.hasExpression())
                            .map(Optional::of);
            case QuerySourceItemForComponentType<?> componentTypedProp ->
                    componentTypedProp.getSubitems().values().stream().map(Optional::of);
            default -> Stream.of(Optional.empty());
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((retrievalModel == null) ? 0 : retrievalModel.hashCode());
        return prime * result + ResultQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery1 && Objects.equal(retrievalModel, ((ResultQuery1) obj).retrievalModel);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .addIfNotNull("retrieval", retrievalModel);
    }

    private static EnhancedYields finaliseYields(final Yields2 originalYields, final EnhancedYields enhancedYields) {
        // TODO: Need to enhance this implementation if a more generic support for write-only components/properties is to be developed.
        //       At this stage only RichText.searchText is handled by removing it from yields, if it was added implicitly.
        //       RichText.searchText shod not be removed if it is yielded explicitly.
        //       This is an optimisation in line with the write-only nature of searchText to avoid its retrieval from a database.
        //       Please also refer to HibernateMappingsGenerator where searchText is mapped to be read as NULL when retrieval happens using Hibernate.
        //
        //       This optimisation applies only to ResultQuery, which is top-level.
        //       It is not applied to SourceQuery and SubQueryForExist, as this job is delegated to DB engines.
        //       For SubQuery, this optimisation simply does not make sense.
        if (!SearchTextYieldFinder.containsSearchText(originalYields)) {
            return SearchTextYieldFinder.findSearchText(enhancedYields.yields)
                    .map(yield -> new EnhancedYields(enhancedYields.yields.removeYield(yield.alias())))
                    .orElse(enhancedYields);
        }
        else {
            return enhancedYields;
        }
    }

    /** Utilities for finding yields that correspond to property `searchText` of {@link RichText}. */
    private static final class SearchTextYieldFinder {

        static Optional<Yield2> findSearchText(final Yields2 yields) {
            return yields.getYields().stream()
                    .filter(SearchTextYieldFinder::isSearchText)
                    .findAny();
        }

        static boolean containsSearchText(final Yields2 yields) {
            return yields.getYields().stream()
                    .anyMatch(SearchTextYieldFinder::isSearchText);
        }

        static boolean isSearchText(final Yield2 yield) {
            return yield.operand() instanceof Prop2 prop && isPathToSearchText(prop.getPath());
        }

        static boolean isPathToSearchText(final List<AbstractQuerySourceItem<?>> path) {
            return path.size() >= 2
                   && path.get(path.size() - 2).javaType() == RichText.class
                   && path.getLast().name.equals(RichText.SEARCH_TEXT);
        }

    }

}

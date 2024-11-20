package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.stage1.*;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.*;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.utils.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.eql.stage1.operands.Prop1.enhancePath;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.extract;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.needsExtraction;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.EMPTY_CONDITIONS;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.conditions;
import static ua.com.fielden.platform.eql.stage2.sundries.GroupBys2.EMPTY_GROUP_BYS;
import static ua.com.fielden.platform.eql.stage2.sundries.OrderBys2.EMPTY_ORDER_BYS;
/**
 * Base class for stage 1 data structures representing an EQL query, suitable for transformation into stage 2.
 * There are four kinds of structures for representing queries depending on its usage:
 * <ol>
 *  <li> {@linkplain ResultQuery1 Result Query} - the most outer query that is used to actually execute to get some data out.
 *  <li> {@linkplain SourceQuery1 Source Query} - a query that is a source for another query (i.e., {@code select from (select ..)}).
 *  <li> {@linkplain SubQuery1 Subquery} - a query that is not used as a source query, but is used in WHERE/ON conditions, yielding, grouping, and ordering.
 *  <li> {@linkplain SubQueryForExists1 Exists Subquery} - a special subquery that doesn't require the yield portion,
 *       which effectively means they are used for EXISTS predicate only.
 * </ol>
 *
 */
public abstract class AbstractQuery1 implements ToString.IFormattable {

    public static final String ERR_CANNOT_FIND_YIELD_FOR_ORDER_BY = "Cannot find yield [%s] used within order by operation.";

    public final Optional<IJoinNode1<? extends IJoinNode2<?>>> maybeJoinRoot;
    public final Conditions1 whereConditions;
    public final Conditions1 udfConditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;

    /** The result type of this query.
     * <ul>
     *   <li> For primitive results -- {@code null}.
     *   <li> For entity results -- an entity type.
     *   <li> For aggregated results -- {@link EntityAggregates}.
     * </ul>
     *  */
    public final Class<? extends AbstractEntity<?>> resultType;

    public final boolean yieldAll;
    public final boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery;

    public AbstractQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        this.maybeJoinRoot = queryComponents.maybeJoinRoot();
        this.whereConditions = queryComponents.whereConditions();
        this.udfConditions = queryComponents.udfConditions();
        this.yields = queryComponents.yields();
        this.groups = queryComponents.groups();
        this.orderings = queryComponents.orderings();
        this.yieldAll = queryComponents.yieldAll();
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = queryComponents.shouldMaterialiseCalcPropsAsColumnsInSqlQuery();
        this.resultType = resultType;
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        maybeJoinRoot.map(ITransformableFromStage1To2::collectEntityTypes).ifPresent(result::addAll);
        result.addAll(whereConditions.collectEntityTypes());
        result.addAll(yields.collectEntityTypes());
        result.addAll(groups.collectEntityTypes());
        result.addAll(orderings.collectEntityTypes());

        return result;
    }

    /**
     * Transforms all query components to stage 2 for the queries with no source.
     *
     * @param context
     * @return
     */
    protected QueryComponents2 transformSourceless(final TransformationContextFromStage1To2 context) {
        return new QueryComponents2(Optional.empty(), whereConditions.transform(context), yields.transform(context), groups.transform(context), orderings.transform(context));
    }

    /**
     * Transforms all query components to stage 2.
     *
     * @param context
     * @return
     */
    protected final QueryComponents2 transformQueryComponents(final TransformationContextFromStage1To2 context,
                                                              final IJoinNode1<? extends IJoinNode2<?>> joinRoot)
    {
        final TransformationResultFromStage1To2<? extends IJoinNode2<?>> joinRootTr = joinRoot.transform(context);
        final TransformationContextFromStage1To2 enhancedContext = joinRootTr.updatedContext;
        final IJoinNode2<? extends IJoinNode3> joinRoot2 = joinRootTr.item;
        final Conditions2 whereConditions2 = enhanceWithUserDataFilterConditions(joinRoot2.mainSource(), context, whereConditions.transform(enhancedContext));
        final Yields2 yields2 = yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, joinRoot2.mainSource());
        // it is important to enhance yields after orderings to enable functioning of 'orderBy().yield(..)' in application to properties rather than true yields
        final Yields2 enhancedYields2 = enhanceYields(yields2, joinRoot2.mainSource());
        return new QueryComponents2(Optional.of(joinRoot2), whereConditions2, enhancedYields2, groups2, orderings2);
    }

    /**
     * Should add implicit yields, if required. This logic depends on a specific query kind.
     *
     * @param yields
     * @param mainSource
     * @return
     */
    abstract protected Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource);

    /**
     * Injects user-defined filtering conditions into the main source WHERE conditions.
     */
    private Conditions2 enhanceWithUserDataFilterConditions(final ISource2<? extends ISource3> mainSource, final TransformationContextFromStage1To2 context, final Conditions2 originalConditions) {
        if (udfConditions.isEmpty()) {
            return originalConditions;
        }

        final TransformationContextFromStage1To2 localContext = TransformationContextFromStage1To2.forMainContext(context).cloneWithAdded(mainSource);
        final Conditions2 udfConditions2 = udfConditions.transform(localContext);

        if (originalConditions.ignore()) {
            return udfConditions2.ignore() ? EMPTY_CONDITIONS : udfConditions2;
        } else {
            return udfConditions2.ignore() ? originalConditions : conditions(false, asList(asList(udfConditions2, originalConditions)));
        }
    }

    protected static GroupBys2 enhance(final GroupBys2 groupBys) {
        if (groupBys.equals(EMPTY_GROUP_BYS)) {
            return EMPTY_GROUP_BYS;
        }

        final List<GroupBy2> enhanced = groupBys.groups().stream().map(group -> enhance(group)).flatMap(List::stream).collect(Collectors.toList());
        return new GroupBys2(enhanced);
    }

    protected static OrderBys2 enhance(final OrderBys2 orderBys, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (orderBys.equals(EMPTY_ORDER_BYS)) {
            return EMPTY_ORDER_BYS;
        }

        final List<OrderBy2> enhanced = new ArrayList<>();

        for (final OrderBy2 original : orderBys.orderBys()) {
            enhanced.addAll(original.operand() != null ? transformForOperand(original.operand(), original.isDesc()) :
                transformForYield(original, yields, mainSource));
        }

        return orderBys.updateOrderBys(enhanced);
    }

    private static List<OrderBy2> transformForYield(final OrderBy2 original, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYieldsMap().containsKey(original.yieldName())) {
            final Yield2 yield = yields.getYieldsMap().get(original.yieldName());
            if (yield.operand() instanceof Prop2 yieldedProp && needsExtraction(yieldedProp.lastPart(), yieldedProp.penultPart())) {
                return transformForOperand(yieldedProp, original.isDesc());
            } else {
                return asList(original);
            }
        }

        if (yields.getYieldsMap().isEmpty()) {
            final PropResolution propResolution = Prop1.resolvePropAgainstSource(mainSource, new Prop1(original.yieldName(), false));
            if (propResolution != null) {
                final List<AbstractQuerySourceItem<?>> path = enhancePath(propResolution.getPath());
                return transformForOperand(new Prop2(mainSource, path), original.isDesc());
            }
        }

        throw new EqlStage1ProcessingException(ERR_CANNOT_FIND_YIELD_FOR_ORDER_BY.formatted(original.yieldName()));
    }

    private static List<OrderBy2> transformForOperand(final ISingleOperand2<?> operand, final boolean isDesc) {
        return operand instanceof Prop2 operandAsProp
                ? extract(operandAsProp).stream().map(keySubprop -> new OrderBy2(keySubprop, isDesc)).collect(toList())
                : asList(new OrderBy2(operand, isDesc));
    }

    private static List<GroupBy2> enhance(final GroupBy2 original) {
        return original.operand() instanceof Prop2 originalOperandAsProp
                ? extract(originalOperandAsProp).stream().map(keySubprop -> new GroupBy2(keySubprop)).collect(toList())
                : asList(original);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + whereConditions.hashCode();
        result = prime * result + udfConditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + orderings.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + maybeJoinRoot.hashCode();
        result = prime * result + yields.hashCode();
        result = prime * result + (yieldAll ? 1231 : 1237);
        result = prime * result + (shouldMaterialiseCalcPropsAsColumnsInSqlQuery ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractQuery1 that
                  && Objects.equals(resultType, that.resultType)
                  && Objects.equals(maybeJoinRoot, that.maybeJoinRoot)
                  && Objects.equals(yields, that.yields)
                  && Objects.equals(whereConditions, that.whereConditions)
                  && Objects.equals(udfConditions, that.udfConditions)
                  && Objects.equals(groups, that.groups)
                  && Objects.equals(orderings, that.orderings)
                  && Objects.equals(yieldAll, that.yieldAll)
                  && Objects.equals(shouldMaterialiseCalcPropsAsColumnsInSqlQuery, that.shouldMaterialiseCalcPropsAsColumnsInSqlQuery);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("resultType", resultType)
                .add("yieldAll", yieldAll)
                .add("shouldMaterialiseCalcPropsAsColumnsInSqlQuery", shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
                .addIfPresent("join", maybeJoinRoot)
                .addIfNot("where", whereConditions, Conditions1::isEmpty)
                .addIfNot("udf", udfConditions, Conditions1::isEmpty)
                .addIfNot("yields", yields, Yields1::isEmpty)
                .addIfNot("groups", groups, GroupBys1::isEmpty)
                .addIfNot("orderings", orderings, OrderBys1::isEmpty)
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}

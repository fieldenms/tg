package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.utils.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

/**
 * A structure used for representing queries in the FROM/JOIN statements.
 * Technically these queries are sub-queries from the point of view of SQL, but the term "sub-query" is reserved for sub-queries in other parts
 * (see {@link SubQuery1}, {@link SubQueryForExists1}).
 *
 * <h3> Source query correlation </h3>
 * A source query can be correlated, which allows it to access values from outer queries, enabling the use of {@code extProp}.
 * All simple queries as source queries can be correlated.
 * Source queries underlying synthetic entities can't be correlated.
 *
 * <h3> Transformation to stage 2 </h3>
 * Yield processing is subject to the following rules:
 * <ul>
 *   <li> In case of no explicit yields or {@code yieldAll}, yields are derived from the main source of the query. No fetch models affect this.
 *   <li> In case of a single unaliased yield when the query result is an entity type, alias ID is used.
 * </ul>
 *
 * @author TG Team
 */
public class SourceQuery1 extends AbstractQuery1 implements ITransformableFromStage1To2<SourceQuery2> {

    public static final String ERR_NO_YIELDS = """
        Nothing can be yielded from a source query: no explicit yields and nothing can be auto-yielded from the query source.
        Either explicit yields should be added, or the query source modified.
        """;
    public final boolean isCorrelated;

    public SourceQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType, final boolean isCorrelated) {
        super(queryComponents, requireNonNull(resultType));
        this.isCorrelated = isCorrelated;
    }

    @Override
    public SourceQuery2 transform(final TransformationContextFromStage1To2 context) {
        final TransformationContextFromStage1To2 localContext = isCorrelated
                ? context
                : context.isForCalcProp
                        ? TransformationContextFromStage1To2.forCalcPropContext(context)
                        : TransformationContextFromStage1To2.forMainContext(context);

        final var queryComponents = maybeJoinRoot.map(joinRoot -> transformQueryComponents(localContext, joinRoot))
                .orElseGet(() -> transformSourceless(localContext));
        return new SourceQuery2(queryComponents, resultType);
    }

    /**
     * Enhances yields according to the following rules:
     * <ol>
     * <li> No yields or {@code yieldAll} - adds properties that belong to {@code mainSource} and satisfy the following:
     *   <ul> Calculated properties are excluded, unless {@link #shouldMaterialiseCalcPropsAsColumnsInSqlQuery} is true
     *        and a calculated property can be materialised.
     *   <li> Other retrievable properties are included.
     *   </ul>
     * <li> A single unaliased yield, when the query result type is a persistent entity, gets alias {@code "id"}.
     * <li> Otherwise, no enhancements are performed.
     * </ol>
     */
    @Override
    protected EnhancedYields enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.isEmpty() || yieldAll) {
            final List<Yield2> enhancedYields = new ArrayList<>(yields.getYields());

            for (final Entry<String, AbstractQuerySourceItem<?>> el : mainSource.querySourceInfo().getProps().entrySet()) {
                // need to materialise only those calc props that are neither implicit, nor entity center totals
                if (!el.getValue().hasExpression() || shouldMaterialiseCalcPropsAsColumnsInSqlQuery && el.getValue().isCalculatedPropertyThatCouldBeMaterialisedAsSqlColumn()) {
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
            final boolean allGenerated = mainSource.querySourceInfo().isComprehensive && yields.isEmpty() && !shouldMaterialiseCalcPropsAsColumnsInSqlQuery;
            // generated yields with shouldMaterialiseCalcPropsAsColumnsInSqlQuery=true will produce different QuerySourceInfo from the canonical one (calc props will be yielded, thus turned from calc to persistent)
            // if necessary additional separate cache can be created for such cases (allGeneratedButWithCalcPropsMaterialised)
            return new EnhancedYields(new Yields2(enhancedYields, allGenerated)) {
                @Override protected String formatErrorEmptyYields() {
                    return ToString.separateLines()
                            .toString(ERR_NO_YIELDS)
                            .add("Calculated properties materialised", shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
                            .add("Source", mainSource.toStringCompact())
                            .$();
                }
            };
        }

        final Yield2 firstYield = yields.getYields().iterator().next();
        if (yields.getYields().size() == 1 && !yieldAll && isEmpty(firstYield.alias()) && isPersistentEntityType(resultType)) {
            return new EnhancedYields(new Yields2(listOf(new Yield2(firstYield.operand(), ID, firstYield.hasNonnullableHint()))));
        }

        return new EnhancedYields(yields);
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

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("isCorrelated", isCorrelated);
    }

}

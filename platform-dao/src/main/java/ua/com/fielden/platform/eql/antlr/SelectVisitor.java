package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlSyntaxException;
import ua.com.fielden.platform.eql.antlr.tokens.AsToken;
import ua.com.fielden.platform.eql.antlr.tokens.JoinToken;
import ua.com.fielden.platform.eql.antlr.tokens.LeftJoinToken;
import ua.com.fielden.platform.eql.antlr.tokens.SelectToken;
import ua.com.fielden.platform.eql.exceptions.EqlStage0ProcessingException;
import ua.com.fielden.platform.eql.meta.EntityTypeInfo;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.*;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.utils.EntityUtils.*;

final class SelectVisitor extends AbstractEqlVisitor<EqlCompilationResult.Select> {

    SelectVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.Select visitSelect(final SelectContext ctx) {
        final YieldsVisitor.Result yieldsResult = compileYields(ctx.selectEnd());

        return new EqlCompilationResult.Select(
                compileJoinRoot(ctx),
                compileWhere(ctx.where()),
                yieldsResult.yields(),
                compileGroups(ctx.groupBy()),
                compileOrderBy(ctx.orderBy()),
                yieldsResult.yieldAll());
    }

    private GroupBys1 compileGroups(final GroupByContext groupByContext) {
        return groupByContext == null
                ? GroupBys1.EMPTY_GROUP_BYS
                : new GroupByVisitor(transformer).visitGroupBy(groupByContext);
    }

    private OrderBys1 compileOrderBy(final OrderByContext orderByContext) {
        return orderByContext == null
                ? OrderBys1.EMPTY_ORDER_BYS
                : new OrderByVisitor(transformer).visitOrderBy(orderByContext);
    }

    private YieldsVisitor.Result compileYields(final SelectEndContext selectEndContext) {
        return switch (selectEndContext) {
            case SelectEnd_AnyYieldContext ctx -> ctx.anyYield().accept(new YieldsVisitor(transformer));
            default -> YieldsVisitor.DEFAULT_RESULT;
        };
    }

    private Optional<IJoinNode1<? extends IJoinNode2<?>>> compileJoinRoot(final SelectContext ctx) {
        final String alias = ctx.alias == null ? null : ((AsToken) ctx.alias).alias;

        final ISource1<?> firstSource;
        final SelectToken token = (SelectToken) ctx.select;
        switch (token) {
            case SelectToken.EntityType tok -> firstSource = compileEntitySource(tok.entityType, alias);
            case SelectToken.Values $ -> {
                // TODO replace null by singleton of new ISource1 or IJoinNode1 subtype
                // we can return early because sourceless selects can't have joins
                return Optional.empty();
            }
            case SelectToken.Models tok -> firstSource = compileModelsSource(tok.models, alias);
        }

        return Optional.of(compileJoin(new JoinLeafNode1(firstSource), ctx.join()));
    }

    /**
     * @param restCtx  the next join context or {@code null}
     */
    private IJoinNode1<? extends IJoinNode2<?>> compileJoin(final IJoinNode1<?> accumulator, final JoinContext restCtx) {
        if (restCtx == null) {
            return accumulator;
        }

        final var conditions = Conditions1.conditions(restCtx.joinCondition().condition().accept(new ConditionVisitor(transformer)));
        final String alias = restCtx.alias == null ? null : ((AsToken) restCtx.alias).alias;
        final var result = compileJoinOperator(restCtx.joinOperator(), alias, (joinType, node) -> {
            return new JoinInnerNode1(accumulator, node, joinType, conditions);
        });
        return compileJoin(result, restCtx.join());
    }

    private <T> T compileJoinOperator(
            final JoinOperatorContext ctx, final String maybeAlias,
            final BiFunction<JoinType, JoinLeafNode1, ? extends T> continuation)
    {
        final JoinType type;
        final JoinLeafNode1 node;

        switch (ctx.token) {
            case LeftJoinToken.EntityType tok -> {
                type = JoinType.LJ;
                node = new JoinLeafNode1(compileEntitySource(tok.entityType, maybeAlias));
            }
            case LeftJoinToken.Models tok -> {
                type = JoinType.LJ;
                node = new JoinLeafNode1(compileModelsSource(tok.models, maybeAlias));
            }
            case JoinToken.EntityType tok -> {
                type = JoinType.IJ;
                node = new JoinLeafNode1(compileEntitySource(tok.entityType, maybeAlias));
            }
            case JoinToken.Models tok -> {
                type = JoinType.IJ;
                node = new JoinLeafNode1(compileModelsSource(tok.models, maybeAlias));
            }
            default -> throw new EqlSyntaxException("Unexpected token: %s".formatted(ctx.token.getText()));
        }

        return continuation.apply(type, node);
    }

    private Source1BasedOnQueries compileModelsSource(final List<? extends QueryModel<?>> models, final String maybeAlias) {
        final var queries = models.stream().map(transformer::generateAsCorrelatedSourceQuery).toList();
        return new Source1BasedOnQueries(queries, maybeAlias, transformer.nextSourceId(), null);
    }

    private ISource1<? extends ISource2<?>> compileEntitySource(final Class<? extends AbstractEntity<?>> entityType, final String alias) {
        if (isPersistedEntityType(entityType)) {
            return new Source1BasedOnPersistentType(entityType, alias, transformer.nextSourceId());
        }
        else if (isSyntheticEntityType(entityType) || isSyntheticBasedOnPersistentEntityType(entityType) || isUnionEntityType(entityType)) {
            return compileSyntheticEntitySource(entityType, alias);
        }
        else {
            throw new EqlStage0ProcessingException("Unexpected entity type used as query source: %s.".formatted(entityType.getName()));
        }
    }

    private ISource1<? extends ISource2<?>> compileSyntheticEntitySource(final Class<? extends AbstractEntity<?>> entityType, final String alias) {
        final EntityTypeInfo<?> parentInfo = getEntityTypeInfo(entityType);
        final List<SourceQuery1> queries = parentInfo.entityModels.stream().map(transformer::generateAsUncorrelatedSourceQuery).toList();
        return new Source1BasedOnQueries(queries, alias, transformer.nextSourceId(), entityType);
    }

    private Conditions1 compileWhere(final WhereContext where) {
        if (where == null) {
            return Conditions1.EMPTY_CONDITIONS;
        }

        final ICondition1<?> condition = where.condition().accept(new ConditionVisitor(transformer));
        return Conditions1.conditions(condition);
    }

}

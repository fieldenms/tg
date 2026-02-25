package ua.com.fielden.platform.eql.retrieval;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.records.QueryModelResult;
import ua.com.fielden.platform.eql.retrieval.records.YieldedColumn;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathsToTreeTransformer;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.IDates;

import java.util.List;
import java.util.Optional;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

/// An entry point for transforming an EQL query to SQL.
///
/// The transformation of EQL into SQL happens in 4 stages:
///
/// 1. **Stage 0: parsing**.
///    A [sequence of EQL tokens][ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource] is transformed into a [stage 1 AST][ResultQuery1].
///    See [ua.com.fielden.platform.eql.antlr.EqlCompiler].
/// 2. **Stage 1: property resolution**.
///    Properties are resolved to their respective sources.
///    An important part of this stage is the resolution of dot-notated properties.
/// 3. **Stage 2**.
///    - Processing of dot-expressions: builds up implicit table joins that result from dot-expressions,
///      substitutes calculated property names used in dot-expressions with their respective expressions.
///    - Substitution of literal values with parameters (crucial for strings as to prevent SQL injection).
///    - Optimisation of union queries with an ordering.
/// 4. **Stage 3: SQL generation**.
///    This stage also gathers the information, needed to instantiate entities from the SQL query result.
///
/// For stages 1-3 there is a corresponding package, named `ua.com.fielden.platform.eql.stage$N`, where `$N` is the stage number.
/// Each package contains classes that comprise a stage-specific AST.
/// These classes are named with a suffix that corresponds to their stage number.
/// For example, `Prop1` represents a property in stage 1, and is a result of stage 0.
///
@Singleton
public final class EqlQueryTransformer {

    private final IFilter filter;
    private final IDates dates;
    private final EqlTables eqlTables;
    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IDomainMetadata domainMetadata;
    private final IDbVersionProvider dbVersionProvider;

    // TODO: Make private once dependent EQL tests are refactored and use IoC.
    @Inject
    public EqlQueryTransformer(
            final IFilter filter,
            final IDates dates,
            final EqlTables eqlTables,
            final QuerySourceInfoProvider querySourceInfoProvider,
            final IDomainMetadata domainMetadata,
            final IDbVersionProvider dbVersionProvider)
    {
        this.filter = filter;
        this.dates = dates;
        this.eqlTables = eqlTables;
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.domainMetadata = domainMetadata;
        this.dbVersionProvider = dbVersionProvider;
    }

    public <E extends AbstractEntity<?>> TransformationResultFromStage2To3<ResultQuery3> transform(
            final QueryProcessingModel<E, ?> qpm,
            final Optional<String> username)
    {
        final var result = transform_(qpm, username);

        if (isForeignIdOnlyQuery(result.item)) {
            final var idOnlyQuery = select((Class<E>) result.item.resultType)
                    .where().prop(ID).in().model((SingleResultQueryModel<?>) qpm.queryModel)
                    .model();
            final var idOnlyQpm = new QueryProcessingModel<>(idOnlyQuery, qpm.orderModel, qpm.fetchModel, qpm.getParamValues(), qpm.lightweight);
            return transform_(idOnlyQpm, username);
        }
        else {
            return result;
        }
    }

    private <E extends AbstractEntity<?>> TransformationResultFromStage2To3<ResultQuery3> transform_(
            final QueryProcessingModel<E, ?> qem,
            final Optional<String> username)
    {
        final QueryModelToStage1Transformer gen = new QueryModelToStage1Transformer(filter, username, new QueryNowValue(dates), qem.getParamValues());
        final ResultQuery1 query1 = gen.generateAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel);

        final TransformationContextFromStage1To2 context1 = TransformationContextFromStage1To2.forMainContext(querySourceInfoProvider, domainMetadata);
        final ResultQuery2 query2 = query1.transform(context1);

        final PathsToTreeTransformer p2tt = new PathsToTreeTransformer(querySourceInfoProvider, domainMetadata, gen);
        final var context2 = new TransformationContextFromStage2To3(p2tt.transformFinally(query2.collectProps()), eqlTables, dbVersionProvider.dbVersion());
        return query2.transform(context2);
    }

    public <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(
            final QueryProcessingModel<E, ?> qem,
            final Optional<String> username)
    {
        final TransformationResultFromStage2To3<ResultQuery3> tr = transform(qem, username);
        final ResultQuery3 entQuery3 = tr.item;
        final String sql = entQuery3.sql(domainMetadata, dbVersionProvider.dbVersion());
        return new QueryModelResult<E>((Class<E>) entQuery3.resultType, sql, getYieldedColumns(entQuery3.yields), tr.updatedContext.getSqlParamValues(), qem.fetchModel);
    }

    private static List<YieldedColumn> getYieldedColumns(final Yields3 model) {
        return model.getYields().stream().map(yield -> new YieldedColumn(yield.alias(), yield.type(), yield.column())).toList();
    }

    /// A "foreign query" is a query whose single explicit yield is an entity-typed property.
    /// This predicate identifies whether `resultQuery` represents a "foreign query".
    ///
    /// An equivalent predicate is present in [EntityContainerFetcherImpl] as part of method [getModelResult][EntityContainerFetcherImpl#getModelResult(QueryProcessingModel)].
    ///
    private static boolean isForeignIdOnlyQuery(final ResultQuery3 resultQuery) {
        final Yield3 yield;
        return isPersistentEntityType(resultQuery.resultType)
               && resultQuery.yields.size() == 1
               && (yield = first(resultQuery.yields.getYields()).orElseThrow())
                  .alias().equals(ID)
               // The type of ID can be either an entity or Long.
               // If it is an entity type, this is a foreign id-only query (a whole entity is being yielded).
               // Otherwise, ID is yielded as a number (a local id-only query).
               && isEntityType(yield.type().javaType());
    }

}

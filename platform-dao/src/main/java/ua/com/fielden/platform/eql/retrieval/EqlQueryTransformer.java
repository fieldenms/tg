package ua.com.fielden.platform.eql.retrieval;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
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
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.IDates;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * An entry point for transforming an EQL query to SQL.
 * <p>
 * There are 3 stages in the transformation from EQL to SQL:
 * <ol>
 * <li> Stage 1 is created from a raw EQL model where a sequence of fluent API calls is transformed into an SQL-like structure.
 * <li> Stage 2 resolves dot-notated properties to their respective sources.
 * <li> Stage 3 builds up all implicit table joins, resulting from dot-notations, substitutes calculated property names used in dot-notations with their respective expressions.
 * </ol>
 * The result of stage 3 is then used to generate the actual SQL select statement. It also includes the information needed to instantiate entities from the SQL query result.
 *
 * @author TG Team
 *
 */
public class EqlQueryTransformer {

    private EqlQueryTransformer() {}

    public static <E extends AbstractEntity<?>> TransformationResultFromStage2To3<ResultQuery3> transform(
            final QueryProcessingModel<E, ?> qem,
            final IFilter filter,
            final Optional<String> username,
            final IDates dates,
            final IDomainMetadata domainMetadata,
            final EqlTables eqlTables,
            final QuerySourceInfoProvider querySourceInfoProvider)
    {
        final QueryModelToStage1Transformer gen = new QueryModelToStage1Transformer(filter, username, new QueryNowValue(dates), qem.getParamValues());
        final ResultQuery1 query1 = gen.generateAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel);

        final TransformationContextFromStage1To2 context1 = TransformationContextFromStage1To2.forMainContext(querySourceInfoProvider);
        final ResultQuery2 query2 = query1.transform(context1);

        final PathsToTreeTransformer p2tt = new PathsToTreeTransformer(querySourceInfoProvider, gen);
        final var context2 = new TransformationContextFromStage2To3(p2tt.transformFinally(query2.collectProps()),
                                                                    domainMetadata, eqlTables);
        return query2.transform(context2);
    }

    protected static <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(
            final QueryProcessingModel<E, ?> qem, final DbVersion dbVersion, final IFilter filter,
            final Optional<String> username, final IDates dates, final IDomainMetadata domainMetadata,
            final EqlTables eqlTables, final QuerySourceInfoProvider querySourceInfoProvider)
    {
        final TransformationResultFromStage2To3<ResultQuery3> tr = transform(qem, filter, username, dates,
                                                                             domainMetadata, eqlTables, querySourceInfoProvider);
        final ResultQuery3 entQuery3 = tr.item;
        final String sql = entQuery3.sql(dbVersion);
        return new QueryModelResult<E>((Class<E>) entQuery3.resultType, sql, getYieldedColumns(entQuery3.yields), tr.updatedContext.getSqlParamValues(), qem.fetchModel);
    }

    private static List<YieldedColumn> getYieldedColumns(final Yields3 model) {
        return unmodifiableList(model.getYields().stream().map(yield -> new YieldedColumn(yield.alias, yield.type, yield.column)).collect(toList()));
    }

}

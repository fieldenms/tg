package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.eql.retrieval.EntityContainerFetcher.getResultPropsInfos;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.QueryModelResult;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage3.QueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.conditions.NullTest3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBy3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBy3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yield3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.operands.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.operands.SubQuery3;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountAll3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesSources3;
import ua.com.fielden.platform.eql.stage3.sources.SingleNodeSources3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.persistence.types.DateTimeType;

public class EqlStage3TestCase extends EqlTestCase {
    public static int sqlId = 0;

    public static int nextSqlId() {
        sqlId = sqlId + 1;
        return sqlId;
    }

    private static final <E extends AbstractEntity<?>> ResultQuery3 transform(final QueryProcessingModel<E, ?> qem) {
        return EqlQueryTransformer.transform(qem, filter, null, dates, metadata()).item;
    }
    private static final <E extends AbstractEntity<?>> QueryModelResult<E> transformToModelResult(final QueryProcessingModel<E, ?> qem) {
        final TransformationResult2<ResultQuery3> tr = EqlQueryTransformer.transform(qem, filter, null, dates, metadata());
        final ResultQuery3 entQuery3 = tr.item;
        final String sql = entQuery3.sql(H2);
        return new QueryModelResult<>((Class<E>) entQuery3.resultType, sql, getResultPropsInfos(entQuery3.yields), tr.updatedContext.getParamValues(), qem.fetchModel);
    }

    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAll(final ICompoundCondition0<T> unfinishedQry) {
        return qry(unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAll(final ICompleted<T> unfinishedQry) {
        return qry(unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate());
    }

    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAllWithUdf(final ICompoundCondition0<T> unfinishedQry) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        countQry.setFilterable(true);
        return qry(countQry);
    }

    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAllWithUdf(final ICompleted<T> unfinishedQry) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        countQry.setFilterable(true);
        return qry(countQry);
    }

    protected static ResultQuery3 qry(final AggregatedResultQueryModel qry) {
        return transform(new QueryProcessingModel<EntityAggregates, AggregatedResultQueryModel>(qry, null, null, emptyMap(), true));
    }

    protected static ResultQuery3 qryFiltered(final AggregatedResultQueryModel qry) {
        qry.setFilterable(true);
        return qry(qry);
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery3 qry(final EntityResultQueryModel<T> qry) {
        return transform(new QueryProcessingModel<T, EntityResultQueryModel<T>>(qry, null, null, emptyMap(), true));
    }

    protected static <T extends AbstractEntity<?>> void assertModelResultsEquals(final EntityResultQueryModel<T> exp, final EntityResultQueryModel<T> act) {
        final QueryModelResult<T> expQmr = transformToModelResult(new QueryProcessingModel<T, EntityResultQueryModel<T>>(exp, null, null, emptyMap(), true));
        final QueryModelResult<T> actQmr = transformToModelResult(new QueryProcessingModel<T, EntityResultQueryModel<T>>(act, null, null, emptyMap(), true));
        
        assertEquals("Qry model results (SQL) are different!", expQmr.getSql(), actQmr.getSql());
        assertEquals("Qry model results (Result Type) are different!", expQmr.getResultType(), actQmr.getResultType());
        assertEquals("Qry model results (Fetch Model) are different!", expQmr.getFetchModel(), actQmr.getFetchModel());
        assertEquals("Qry model results (Param values) are different!", expQmr.getParamValues(), actQmr.getParamValues());
        assertEquals("Qry model results (Yielded props infos) are different!", expQmr.getYieldedPropsInfo(), actQmr.getYieldedPropsInfo());
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery3 qryFiltered(final EntityResultQueryModel<T> qry) {
        qry.setFilterable(true);
        return qry(qry);
    }

    
    protected static Source3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final Integer sourceForContextId) {
        return new Source3BasedOnTable(tables.get(sourceType.getName()), sourceForContextId, nextSqlId());
    }

    protected static Source3BasedOnSubqueries source(final Integer sourceForContextId, final SourceQuery3... sourceQueries) {
        return new Source3BasedOnSubqueries(Arrays.asList(sourceQueries), sourceForContextId, nextSqlId());
    }

    protected static Expression3 expr(final ISingleOperand3 op1, final Class<?> type, final Object hibType) {
        return new Expression3(op1, emptyList(), type, hibType);
    }

    protected static ISingleOperand3 prop(final String name, final ISource3 source) {
        return new Prop3(name, source, null, null);
    }

    protected static ISingleOperand3 entityProp(final String name, final ISource3 source, final Class<? extends AbstractEntity<?>> entityType) {
        return new Prop3(name, source, entityType, LongType.INSTANCE);
    }

    protected static ISingleOperand3 idProp(final ISource3 source) {
        return new Prop3(ID, source, Long.class, LongType.INSTANCE);
    }

    protected static ISingleOperand3 stringProp(final String name, final ISource3 source) {
        return new Prop3(name, source, String.class, StringType.INSTANCE);
    }

    protected static ISingleOperand3 prop(final String name, final ISource3 source, final Class<?> type, final Type hibType) {
        return new Prop3(name, source, type, hibType);
    }

    protected static ISingleOperand3 dateProp(final String name, final ISource3 source) {
        return new Prop3(name, source, Date.class, DateTimeType.INSTANCE);
    }

    protected static ComparisonTest3 eq(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, EQ, op2);
    }

    protected static ComparisonTest3 ne(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, NE, op2);
    }

    protected static NullTest3 isNotNull(final ISingleOperand3 op1) {
        return new NullTest3(op1, true);
    }

    protected static NullTest3 isNull(final ISingleOperand3 op1) {
        return new NullTest3(op1, false);
    }

    protected static Conditions3 cond(final ICondition3 condition) {
        return new Conditions3(false, asList(asList(condition)));
    }

    protected static ISources3 sources(final ISources3 main, final JoinType jt, final ISources3 second, final Conditions3 conditions) {
        return new MultipleNodesSources3(main, second, jt, conditions);
    }

    protected static ISources3 sources(final ISource3 main, final JoinType jt, final ISources3 second, final Conditions3 conditions) {
        return new MultipleNodesSources3(sources(main), second, jt, conditions);
    }

    protected static ISources3 sources(final ISources3 main, final JoinType jt, final ISource3 second, final Conditions3 conditions) {
        return new MultipleNodesSources3(main, sources(second), jt, conditions);
    }

    protected static ISources3 sources(final ISource3 main, final JoinType jt, final ISource3 second, final Conditions3 conditions) {
        return new MultipleNodesSources3(sources(main), sources(second), jt, conditions);
    }

    protected static ISources3 sources(final ISources3 main, final JoinType jt, final ISources3 second, final ICondition3 condition) {
        return new MultipleNodesSources3(main, second, jt, cond(condition));
    }

    protected static ISources3 sources(final ISource3 main, final JoinType jt, final ISources3 second, final ICondition3 condition) {
        return new MultipleNodesSources3(sources(main), second, jt, cond(condition));
    }

    protected static ISources3 sources(final ISources3 main, final JoinType jt, final ISource3 second, final ICondition3 condition) {
        return new MultipleNodesSources3(main, sources(second), jt, cond(condition));
    }

    protected static ISources3 sources(final ISource3 main, final JoinType jt, final ISource3 second, final ICondition3 condition) {
        return new MultipleNodesSources3(sources(main), sources(second), jt, cond(condition));
    }

    protected static ISources3 lj(final ISources3 main, final ISources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static ISources3 lj(final ISource3 main, final ISources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static ISources3 lj(final ISources3 main, final ISource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static ISources3 lj(final ISource3 main, final ISource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static ISources3 lj(final ISources3 main, final ISources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static ISources3 lj(final ISource3 main, final ISources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static ISources3 lj(final ISources3 main, final ISource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static ISources3 lj(final ISource3 main, final ISource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static ISources3 ij(final ISources3 main, final ISources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static ISources3 ij(final ISource3 main, final ISources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static ISources3 ij(final ISources3 main, final ISource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static ISources3 ij(final ISource3 main, final ISource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static ISources3 ij(final ISources3 main, final ISources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static ISources3 ij(final ISource3 main, final ISources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static ISources3 ij(final ISources3 main, final ISource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static ISources3 ij(final ISource3 main, final ISource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static ISources3 sources(final ISource3 main) {
        return new SingleNodeSources3(main);
    }

    //    private static EntQuery3 qry(final IQrySources3 sources, final QueryCategory queryCategory, final Class<?> resultType) {
    //        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(false, emptyList()), yields(), groups(), orders()), queryCategory, resultType);
    //    }
    //
    //    private static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final QueryCategory queryCategory, final Class<?> resultType) {
    //        return new EntQuery3(new EntQueryBlocks3(sources, conditions, yields(), groups(), orders()), queryCategory, resultType);
    //    }
    //
    //    private static EntQuery3 qry(final IQrySources3 sources, final Yields3 yields, final QueryCategory queryCategory, final Class<?> resultType) {
    //        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(false, emptyList()), yields, groups(), orders()), queryCategory, resultType);
    //    }

    protected static SubQuery3 subqry(final ISources3 sources, final Yields3 yields, final Class<?> resultType, final Type hibType) {
        return new SubQuery3(new QueryBlocks3(sources, null, yields, null, null), resultType, hibType);
    }

    protected static SubQuery3 subqry(final ISources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType, final Type hibType) {
        return new SubQuery3(new QueryBlocks3(sources, conditions, yields, null, null), resultType, hibType);
    }

    private static ResultQuery3 resultQry(final ISources3 sources, final Yields3 yields, final Class<?> resultType) {
        return new ResultQuery3(new QueryBlocks3(sources, null, yields, null, null), resultType);
    }

    private static ResultQuery3 resultQry(final ISources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
        return new ResultQuery3(new QueryBlocks3(sources, conditions, yields, null, null), resultType);
    }

    //    private static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final QueryCategory queryCategory, final Class<?> resultType) {
    //        return new EntQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), queryCategory, resultType);
    //    }

    private static SourceQuery3 sourceQry(final ISources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
        return new SourceQuery3(new QueryBlocks3(sources, conditions, yields, null, null), resultType);
    }

    //    protected static EntQuery3 qry(final IQrySources3 sources, final Class<?> resultType) {
    //        return qry(sources, RESULT_QUERY, resultType);
    //    }
    //
    //    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Class<?> resultType) {
    //        return qry(sources, conditions, RESULT_QUERY, resultType);
    //    }

    protected static ResultQuery3 qryCountAll(final ISources3 sources, final Conditions3 conditions) {
        return resultQry(sources, conditions, yields(yieldCountAll("KOUNT")), EntityAggregates.class);
    }

    protected static ResultQuery3 qryCountAll(final ISources3 sources) {
        return resultQry(sources, null, yields(yieldCountAll("KOUNT")), EntityAggregates.class);
    }

    protected static ResultQuery3 qry(final ISources3 sources, final Yields3 yields, final Class<?> resultType) {
        return resultQry(sources, yields, resultType);
    }

    protected static ResultQuery3 qry(final ISources3 sources, final Yields3 yields) {
        return qry(sources, yields, EntityAggregates.class);
    }

    //    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
    //        return qry(sources, conditions, yields, RESULT_QUERY, resultType);
    //    }
    //
    //    protected static EntQuery3 srcqry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
    //        return qry(sources, conditions, yields, QueryCategory.SOURCE_QUERY, resultType);
    //    }

    protected static SourceQuery3 srcqry(final ISources3 sources, final Conditions3 conditions, final Yields3 yields) {
        return sourceQry(sources, conditions, yields, EntityAggregates.class);
    }

    //    protected static EntQuery3 subqry(final IQrySources3 sources, final Class<?> resultType) {
    //        return qry(sources, SUB_QUERY, resultType);
    //    }
    //
    //    protected static EntQuery3 subqry(final IQrySources3 sources, final Conditions3 conditions, final Class<?> resultType) {
    //        return qry(sources, conditions, SUB_QUERY, resultType);
    //    }

    protected static Yields3 yields(final Yield3... yields) {
        return new Yields3(asList(yields));
    }

    protected static Yield3 yieldCountAll(final String alias) {
        return new Yield3(CountAll3.INSTANCE, alias, nextSqlId(), false, INTEGER, H_INTEGER);
    }

    protected static Yield3 yieldEntity(final String propName, final ISource3 source, final String alias, final Class<? extends AbstractEntity<?>> propType) {
        return new Yield3(entityProp(propName, source, propType), alias, nextSqlId(), false, propType, H_LONG);
    }

    protected static Yield3 yieldString(final String propName, final ISource3 source, final String alias) {
        return new Yield3(stringProp(propName, source), alias, nextSqlId(), false, String.class, H_STRING);
    }

    protected static Yield3 yieldId(final ISource3 source, final String alias) {
        return new Yield3(idProp(source), alias, nextSqlId(), false, Long.class, H_LONG);
    }

    protected static Yield3 yieldProp(final String propName, final ISource3 source, final String alias, final Class<?> type, final Type hibType) {
        return new Yield3(prop(propName, source, type, hibType), alias, nextSqlId(), false, type, hibType);
    }

    protected static Yield3 yieldProp(final String propName, final ISource3 source, final String alias) {
        return new Yield3(prop(propName, source), alias, nextSqlId(), false, null, null);
    }

    protected static Yield3 yieldModel(final SubQuery3 model, final String alias, final Class<?> type, final Type hibType) {
        return new Yield3(model, alias, nextSqlId(), false, type, hibType);
    }

    protected static Yield3 yieldSingleEntity(final String propName, final ISource3 source, final Class<? extends AbstractEntity<?>> propType) {
        return yieldEntity(propName, source, "", propType);
    }

    protected static Yield3 yieldSingleString(final String propName, final ISource3 source) {
        return yieldString(propName, source, "");
    }

    protected static Yield3 yieldSingleProp(final String propName, final ISource3 source) {
        return yieldProp(propName, source, "");
    }

    protected static GroupBys3 groups(final GroupBy3... groups) {
        return new GroupBys3(asList(groups));
    }

    protected static OrderBys3 orders(final OrderBy3... orders) {
        return new OrderBys3(asList(orders));
    }

    protected static List<? extends ICondition3> and(final ICondition3... conditions) {
        return asList(conditions);
    }

    protected static Conditions3 or(final ICondition3... conditions) {
        final List<List<? extends ICondition3>> list = new ArrayList<>();
        for (final ICondition3 cond : conditions) {
            list.add(and(cond));
        }
        return new Conditions3(false, list);
    }

    @SafeVarargs
    protected static Conditions3 or(final List<? extends ICondition3>... conditions) {
        return new Conditions3(false, asList(conditions));
    }
}
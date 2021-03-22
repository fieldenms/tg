package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformator;
import ua.com.fielden.platform.eql.stage2.TablesAndSourceChildren;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage3.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.conditions.NullTest3;
import ua.com.fielden.platform.eql.stage3.core.GroupBy3;
import ua.com.fielden.platform.eql.stage3.core.GroupBys3;
import ua.com.fielden.platform.eql.stage3.core.OrderBy3;
import ua.com.fielden.platform.eql.stage3.core.OrderBys3;
import ua.com.fielden.platform.eql.stage3.core.Yield3;
import ua.com.fielden.platform.eql.stage3.core.Yields3;
import ua.com.fielden.platform.eql.stage3.functions.CountAll3;
import ua.com.fielden.platform.eql.stage3.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.operands.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.operands.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesQrySources3;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sources.SingleNodeQrySources3;
import ua.com.fielden.platform.persistence.types.DateTimeType;

public class EqlStage3TestCase extends EqlTestCase {
    public static Type dateTimeHibType = new DateTimeType();
    public static int sqlId = 0;
    
    public static int nextSqlId() {
        sqlId = sqlId + 1;
        return sqlId;
    }

    
    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAll(final ICompoundCondition0<T> unfinishedQry) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();

        final PropsResolutionContext resolutionContext = new PropsResolutionContext(metadata());
        final EntQueryGenerator qb = qb();
        final ResultQuery2 rq2 = qb.generateEntQueryAsResultQuery(countQry, null, null).transform(resolutionContext);
        final PathsToTreeTransformator pathsToTreeTransformator = new PathsToTreeTransformator( metadata(), qb);
        final ua.com.fielden.platform.eql.stage2.TransformationResult<ResultQuery3> s2tr = rq2.transform(new TransformationContext(new TablesAndSourceChildren(tables, pathsToTreeTransformator.groupChildren(rq2.collectProps()))));
        return s2tr.item;
    }
    
    protected static ResultQuery3 qry(final AggregatedResultQueryModel qry) {
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(metadata());
        final EntQueryGenerator qb = qb();
        final ResultQuery2 rq2 = qb.generateEntQueryAsResultQuery(qry, null, null).transform(resolutionContext);
        final PathsToTreeTransformator pathsToTreeTransformator = new PathsToTreeTransformator( metadata(), qb);
        final ua.com.fielden.platform.eql.stage2.TransformationResult<ResultQuery3> s2tr = rq2.transform(new TransformationContext(new TablesAndSourceChildren(tables, pathsToTreeTransformator.groupChildren(rq2.collectProps()))));
        return s2tr.item;
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery3 qryCountAll(final ICompoundCondition0<T> unfinishedQry, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();

        final PropsResolutionContext resolutionContext = new PropsResolutionContext(metadata());
        final EntQueryGenerator qb = qb(paramValues);
        final ResultQuery2 rq2 = qb.generateEntQueryAsResultQuery(countQry, null, null).transform(resolutionContext);
        final PathsToTreeTransformator pathsToTreeTransformator = new PathsToTreeTransformator( metadata(), qb);
        final ua.com.fielden.platform.eql.stage2.TransformationResult<ResultQuery3> s2tr = rq2.transform(new TransformationContext(new TablesAndSourceChildren(tables, pathsToTreeTransformator.groupChildren(rq2.collectProps()))));
        return s2tr.item;
    }

    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final String sourceForContextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), sourceForContextId, 0);
    }

    protected static QrySource3BasedOnSubqueries source(final String sourceForContextId, final SourceQuery3 ... sourceQueries) {
        return new QrySource3BasedOnSubqueries(Arrays.asList(sourceQueries), sourceForContextId, 0);
    }
    
    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final String sourceForContextId, final String subcontextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), sourceForContextId + "_" + subcontextId, 0);
    }

    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final QrySource3BasedOnTable sourceForContextId, final String subcontextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), sourceForContextId.contextId + "_" + subcontextId, 0);
    }
    
    protected static Expression3 expr(final ISingleOperand3 op1) {
        return new Expression3(op1);
    }

    protected static ISingleOperand3 prop(final String name, final IQrySource3 source) {
        return new EntProp3(name, source, null, null);
    }
    
    protected static ISingleOperand3 entityProp(final String name, final IQrySource3 source, final Class<? extends AbstractEntity<?>> entityType) {
        return new EntProp3(name, source, entityType, LongType.INSTANCE);
    }

    protected static ISingleOperand3 idProp(final IQrySource3 source) {
        return new EntProp3(ID, source, Long.class, LongType.INSTANCE);
    }

    
    protected static ISingleOperand3 stringProp(final String name, final IQrySource3 source) {
        return new EntProp3(name, source, String.class, StringType.INSTANCE);
    }

    protected static ISingleOperand3 prop(final String name, final IQrySource3 source, final Class<?> type, final Type hibType) {
        return new EntProp3(name, source, type, hibType);
    }

    
    protected static ISingleOperand3 dateProp(final String name, final IQrySource3 source) {
        return new EntProp3(name, source, Date.class, dateTimeHibType);
    }
    
    protected static ComparisonTest3 eq(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, EQ, op2);
    }
    
    protected static ComparisonTest3 ne(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, NE, op2);
    }
        
    protected static Conditions3 isNotNull(final ISingleOperand3 op1) {
        return cond(new NullTest3(op1, true));
    }

    protected static Conditions3 isNull(final ISingleOperand3 op1) {
        return cond(new NullTest3(op1, false));
    }
    
    protected static NullTest3 isNotNullSingle(final ISingleOperand3 op1) {
        return new NullTest3(op1, true);
    }

    protected static NullTest3 isNullSingle(final ISingleOperand3 op1) {
        return new NullTest3(op1, false);
    }

    protected static Conditions3 cond(final ICondition3 condition) {
        return new Conditions3(false, asList(asList(condition)));
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySources3 second, final Conditions3 conditions) {
        return new MultipleNodesQrySources3(main, second, jt, conditions);
    }

    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySources3 second, final Conditions3 conditions) {
        return new MultipleNodesQrySources3(sources(main), second, jt, conditions);
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySource3 second, final Conditions3 conditions) {
        return new MultipleNodesQrySources3(main, sources(second), jt, conditions);
    }
    
    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final Conditions3 conditions) {
        return new MultipleNodesQrySources3(sources(main), sources(second), jt, conditions);
    }
    
    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySources3 second, final ICondition3 condition) {
        return new MultipleNodesQrySources3(main, second, jt, cond(condition));
    }

    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySources3 second, final ICondition3 condition) {
        return new MultipleNodesQrySources3(sources(main), second, jt, cond(condition));
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySource3 second, final ICondition3 condition) {
        return new MultipleNodesQrySources3(main, sources(second), jt, cond(condition));
    }
    
    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final ICondition3 condition) {
        return new MultipleNodesQrySources3(sources(main), sources(second), jt, cond(condition));
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static IQrySources3 lj(final IQrySource3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }
    
    protected static IQrySources3 lj(final IQrySource3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }
    
    protected static IQrySources3 lj(final IQrySources3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 lj(final IQrySource3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }
    
    protected static IQrySources3 lj(final IQrySource3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static IQrySources3 ij(final IQrySource3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }
    
    protected static IQrySources3 ij(final IQrySource3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }
    
    protected static IQrySources3 ij(final IQrySources3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySource3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }
    
    protected static IQrySources3 ij(final IQrySource3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 sources(final IQrySource3 main) {
        return new SingleNodeQrySources3(main);
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

    protected static SubQuery3 subqry(final IQrySources3 sources, final Yields3 yields, final Class<?> resultType) {
        return new SubQuery3(new EntQueryBlocks3(sources, new Conditions3(false, emptyList()), yields, groups(), orders()), resultType);
    }

    protected static SubQuery3 subqry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
        return new SubQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), resultType);
    }

    private static ResultQuery3 resultQry(final IQrySources3 sources, final Yields3 yields, final Class<?> resultType) {
        return new ResultQuery3(new EntQueryBlocks3(sources, new Conditions3(false, emptyList()), yields, groups(), orders()), resultType);
    }

    private static ResultQuery3 resultQry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
        return new ResultQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), resultType);
    }

//    private static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final QueryCategory queryCategory, final Class<?> resultType) {
//        return new EntQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), queryCategory, resultType);
//    }

    private static SourceQuery3 sourceQry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
        return new SourceQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), resultType);
    }

//    protected static EntQuery3 qry(final IQrySources3 sources, final Class<?> resultType) {
//        return qry(sources, RESULT_QUERY, resultType);
//    }
//
//    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Class<?> resultType) {
//        return qry(sources, conditions, RESULT_QUERY, resultType);
//    }

    protected static ResultQuery3 qryCountAll(final IQrySources3 sources, final Conditions3 conditions) {
        return resultQry(sources, conditions, yields(yieldCountAll("KOUNT")), EntityAggregates.class);
    }

    protected static ResultQuery3 qryCountAll(final IQrySources3 sources) {
        return resultQry(sources, new Conditions3(false, emptyList()), yields(yieldCountAll("KOUNT")), EntityAggregates.class);
    }
    
    protected static ResultQuery3 qry(final IQrySources3 sources, final Yields3 yields, final Class<?> resultType) {
        return resultQry(sources, yields, resultType);
    }
    
    protected static ResultQuery3 qry(final IQrySources3 sources, final Yields3 yields) {
        return qry(sources, yields, EntityAggregates.class);
    }

//    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
//        return qry(sources, conditions, yields, RESULT_QUERY, resultType);
//    }
//
//    protected static EntQuery3 srcqry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final Class<?> resultType) {
//        return qry(sources, conditions, yields, QueryCategory.SOURCE_QUERY, resultType);
//    }

    protected static SourceQuery3 srcqry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields) {
        return sourceQry(sources, conditions, yields, EntityAggregates.class);
    }

//    protected static EntQuery3 subqry(final IQrySources3 sources, final Class<?> resultType) {
//        return qry(sources, SUB_QUERY, resultType);
//    }
//
//    protected static EntQuery3 subqry(final IQrySources3 sources, final Conditions3 conditions, final Class<?> resultType) {
//        return qry(sources, conditions, SUB_QUERY, resultType);
//    }

    protected static Yields3 yields(final Yield3 ... yields) {
        return new Yields3(asList(yields));
    }

    protected static Yield3 yieldCountAll(final String alias) {
        return new Yield3(new CountAll3(), alias, nextSqlId(), false, null, null);
    }
    
    protected static Yield3 yieldExpr(final String propName, final IQrySource3 source, final String alias) {
        return new Yield3(expr(prop(propName, source)), alias, nextSqlId(), false, null, null);
    }

    protected static Yield3 yieldEntityExpr(final String propName, final IQrySource3 source, final String alias, final Class<? extends AbstractEntity<?>> propType) {
        return new Yield3(expr(entityProp(propName, source, propType)), alias, nextSqlId(), false, propType, H_LONG);
    }

    protected static Yield3 yieldStringExpr(final String propName, final IQrySource3 source, final String alias) {
        return new Yield3(expr(stringProp(propName, source)), alias, nextSqlId(), false, String.class, H_STRING);
    }

    protected static Yield3 yieldIdExpr(final IQrySource3 source, final String alias) {
        return new Yield3(expr(idProp(source)), alias, nextSqlId(), false, Long.class, H_LONG);
    }
    
    protected static Yield3 yieldPropExpr(final String propName, final IQrySource3 source, final String alias, final Class<?> type, final Type hibType) {
        return new Yield3(expr(prop(propName, source, type, hibType)), alias, nextSqlId(), false, type, hibType);
    }

    protected static Yield3 yieldProp(final String propName, final IQrySource3 source, final String alias) {
        return new Yield3(prop(propName, source), alias, nextSqlId(), false, null, null);
    }
    
    protected static Yield3 yieldModel(final SubQuery3 model, final String alias) {
        return new Yield3(model, alias, nextSqlId(), false, null, null);
    }

    protected static Yield3 yieldSingleExpr(final String propName, final IQrySource3 source, final Class<? extends AbstractEntity<?>> propType) {
        return yieldEntityExpr(propName, source, "", propType);
    }

    protected static Yield3 yieldSingleStringExpr(final String propName, final IQrySource3 source) {
        return yieldStringExpr(propName, source, "");
    }

    protected static Yield3 yieldSingleExpr(final String propName, final IQrySource3 source) {
        return yieldExpr(propName, source, "");
    }

    
    protected static Yield3 yieldSingleProp(final String propName, final IQrySource3 source) {
        return yieldProp(propName, source, "");
    }

    protected static GroupBys3 groups(final GroupBy3 ... groups) {
        return new GroupBys3(asList(groups));
    }

    protected static OrderBys3 orders(final OrderBy3 ... orders) {
        return new OrderBys3(asList(orders));
    }

    protected static List<? extends ICondition3> and(final ICondition3 ... conditions) {
        return asList(conditions);
    }

    protected static Conditions3 or(final ICondition3 ... conditions) {
        final List<List<? extends ICondition3>> list = new ArrayList<>();
        for (final ICondition3 cond : conditions) {
            list.add(and(cond));
        }
        return new Conditions3(false, list);
    }

    @SafeVarargs
    protected static Conditions3 or(final List<? extends ICondition3> ... conditions) {
        return new Conditions3(false, asList(conditions));
    }
}
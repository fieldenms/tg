package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.emptyConditions;
import static ua.com.fielden.platform.eql.stage2.etc.GroupBys2.emptyGroupBys;
import static ua.com.fielden.platform.eql.stage2.etc.OrderBys2.emptyOrderBys;
import static ua.com.fielden.platform.eql.stage2.etc.Yields2.emptyYields;
import static ua.com.fielden.platform.eql.stage2.etc.Yields2.nullYields;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.ComparisonPredicate2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ExistencePredicate2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.conditions.NullPredicate2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBy2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.functions.CountAll2;
import ua.com.fielden.platform.eql.stage2.operands.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SubQueryForExists2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.JoinInnerNode2;
import ua.com.fielden.platform.eql.stage2.sources.JoinLeafNode2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public abstract class EqlStage2TestCase extends EqlTestCase {

    protected static AbstractQuerySourceItem<?> pi(final Class<?> type, final String propName) {
        return querySourceInfoProvider().getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) type).getProps().get(propName);
    }

    protected static AbstractQuerySourceItem<?> pi(final Class<?> type, final String propName, final String subPropName) {
        final AbstractQuerySourceItem<?> querySourceInfoItem = querySourceInfoProvider().getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) type).getProps().get(propName);
        if (querySourceInfoItem instanceof QuerySourceItemForComponentType) {
            return (AbstractQuerySourceItem<?>) ((QuerySourceItemForComponentType<?>) querySourceInfoItem).getSubitems().get(subPropName);
        } else if (querySourceInfoItem instanceof QuerySourceItemForUnionType) {
            return (AbstractQuerySourceItem<?>) ((QuerySourceItemForUnionType<?>) querySourceInfoItem).getProps().get(subPropName);
        } else {
            throw new EqlException("Can't obtain metadata for property " + propName + " and subproperty " + subPropName + " within type " + type);
        }
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qryCountAll(final ICompoundCondition0<T> unfinishedQry) {
        return qryCountAll(unfinishedQry, emptyMap());
    }

    protected static <T extends AbstractEntity<?>> T2<QueryModelToStage1Transformer, ResultQuery2> qryCountAll2(final ICompoundCondition0<T> unfinishedQry) {
        return qryCountAll2(unfinishedQry, emptyMap());
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qryCountAll(final ICompoundCondition0<T> unfinishedQry, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        final TransformationContext1 context = new TransformationContext1(querySourceInfoProvider(), false);
        return qb(paramValues).generateAsResultQuery(countQry, null, null).transform(context);
    }

    protected static <T extends AbstractEntity<?>> T2<QueryModelToStage1Transformer, ResultQuery2> qryCountAll2(final ICompoundCondition0<T> unfinishedQry, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        final TransformationContext1 context = new TransformationContext1(querySourceInfoProvider(), false);
        final QueryModelToStage1Transformer qb = qb(paramValues);
        return t2(qb, qb.generateAsResultQuery(countQry, null, null).transform(context));
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qry(final EntityResultQueryModel<T> qry) {
        return qry(qry, null);
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qry(final EntityResultQueryModel<T> qry, final OrderingModel order) {
        final TransformationContext1 context = new TransformationContext1(querySourceInfoProvider(), false);
        return qb().generateAsResultQuery(qry, order, new EntityRetrievalModel<T>(EntityQueryUtils.fetch(qry.getResultType()), DOMAIN_METADATA_ANALYSER)).transform(context);
    }

    protected static ResultQuery2 qry(final AggregatedResultQueryModel qry, final OrderingModel order) {
        final TransformationContext1 context = new TransformationContext1(querySourceInfoProvider(), false);
        return qb().generateAsResultQuery(qry, order, null).transform(context);
    }

    protected static ResultQuery2 qry(final AggregatedResultQueryModel qry) {
        final TransformationContext1 context = new TransformationContext1(querySourceInfoProvider(), false);
        return qb().generateAsResultQuery(qry, null, null).transform(context);
    }

    protected static QueryComponents2 qc2(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions, final Yields2 yields) {
        return new QueryComponents2(sources, conditions, yields, emptyGroupBys, emptyOrderBys);
    }

    protected static QueryComponents2 qc2(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions, final Yields2 yields, final OrderBys2 orderBys) {
        return new QueryComponents2(sources, conditions, yields, emptyGroupBys, orderBys);
    }

    protected static Yields2 mkYields(final Yield2... yields) {
        if (yields.length > 0) {
            return new Yields2(asList(yields)); 
        } else {
            return emptyYields;
        }
    }

    protected static OrderBy2 orderDesc(final Prop2 prop) {
        return new OrderBy2(prop, true);
    }

    protected static OrderBy2 orderDesc(final String yieldName) {
        return new OrderBy2(yieldName, true);
    }

    protected static OrderBy2 orderAsc(final Prop2 prop) {
        return new OrderBy2(prop, false);
    }

    protected static OrderBys2 orderBys(final OrderBy2... orderBys) {
        if (orderBys.length > 0) {
            return new OrderBys2(asList(orderBys));    
        } else {
            return emptyOrderBys;
        }
    }

    protected static Yield2 yieldCountAll(final String alias) {
        return new Yield2(CountAll2.INSTANCE, alias, false);
    }

    protected static Yield2 mkYield(final ISingleOperand2<? extends ISingleOperand3> operand, final String alias) {
        return new Yield2(operand, alias, false);
    }

    protected static Prop2 prop(final ISource2<? extends ISource3> source, final AbstractQuerySourceItem<?>... querySourceInfoItems) {
        return new Prop2(source, asList(querySourceInfoItems));
    }

    protected static Prop2 propWithIsId(final ISource2<? extends ISource3> source, final AbstractQuerySourceItem<?>... querySourceInfoItems) {
        return new Prop2(source, asList(querySourceInfoItems), true);
    }

    protected static Conditions2 cond(final ICondition2<? extends ICondition3> condition) {
        return new Conditions2(false, asList(asList(condition)));
    }

    //    protected static Conditions1 conditions(final ICondition1<?> firstCondition, final CompoundCondition1... otherConditions) {
    //        return new Conditions1(false, firstCondition, asList(otherConditions));
    //    }
    //
    //    protected static Conditions1 conditions(final ICondition1<?> firstCondition) {
    //        return new Conditions1(false, firstCondition, emptyList());
    //    }

    protected static IJoinNode2<? extends IJoinNode3> sources(final ISource2<? extends ISource3> main) {
        return new JoinLeafNode2(main);
    }

    protected static IJoinNode2<? extends IJoinNode3> ij(final IJoinNode2<? extends IJoinNode3> leftSource, final ISource2<? extends ISource3> rightSource, final Conditions2 conditions) {
        return new JoinInnerNode2(leftSource, new JoinLeafNode2(rightSource), IJ, conditions);
    }

    protected static IJoinNode2<? extends IJoinNode3> ij(final ISource2<? extends ISource3> leftSource, final ISource2<? extends ISource3> rightSource, final Conditions2 conditions) {
        return new JoinInnerNode2(new JoinLeafNode2(leftSource), new JoinLeafNode2(rightSource), IJ, conditions);
    }
    
    //    protected static CompoundSource1 lj(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
    //        return new CompoundSource1(source, LJ, conditions(firstCondition));
    //    }
    //
    //    protected static CompoundSource1 ij(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
    //        return new CompoundSource1(source, IJ, conditions(firstCondition));
    //    }
    //    
    //    protected static CompoundCondition1 and(final ICondition1<?> condition) {
    //        return new CompoundCondition1(AND, condition);
    //    }
    //
    //    protected static CompoundCondition1 or(final ICondition1<?> condition) {
    //        return new CompoundCondition1(OR, condition);
    //    }

    protected static List<? extends ICondition2<?>> and(final ICondition2<?>... conditions) {
        return asList(conditions);
    }

    protected static Conditions2 or(final ICondition2<?>... conditions) {
        final List<List<? extends ICondition2<?>>> list = new ArrayList<>();
        for (final ICondition2<?> cond : conditions) {
            list.add(and(cond));
        }
        return new Conditions2(false, list);
    }

    @SafeVarargs
    protected static Conditions2 or(final List<? extends ICondition2<?>>... conditions) {
        return new Conditions2(false, asList(conditions));
    }

    protected static ExistencePredicate2 exists(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions) {
        return new ExistencePredicate2(false, typelessSubqry(sources, conditions));
    }

    protected static ExistencePredicate2 notExists(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions) {
        return new ExistencePredicate2(true, typelessSubqry(sources, conditions));
    }

    protected static NullPredicate2 isNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return new NullPredicate2(operand, false);
    }

    protected static NullPredicate2 isNotNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return new NullPredicate2(operand, true);
    }

    protected static ComparisonPredicate2 eq(final Prop2 op1, final Prop2 op2) {
        return new ComparisonPredicate2(op1, EQ, op2);
    }

    protected static ComparisonPredicate2 ne(final Prop2 op1, final Prop2 op2) {
        return new ComparisonPredicate2(op1, NE, op2);
    }

    protected static Prop1 prop(final String name) {
        return new Prop1(name, false);
    }

    protected static Source2BasedOnPersistentType source(final Integer id, final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return new Source2BasedOnPersistentType(querySourceInfoProvider().getModelledQuerySourceInfo(sourceType), alias, id, true, false);
    }

    protected static Source2BasedOnPersistentType source(final Integer id, final Class<? extends AbstractEntity<?>> sourceType) {
        return new Source2BasedOnPersistentType(querySourceInfoProvider().getModelledQuerySourceInfo(sourceType), id, true, false);
    }

    protected static Source2BasedOnQueries source(final QuerySourceInfo<?> querySourceInfo, final Integer id, final SourceQuery2... queries) {
        return new Source2BasedOnQueries(Arrays.asList(queries), null, id, querySourceInfo, false, true, false);
    }

    protected static ResultQuery2 qryCountAll(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions) {
        return new ResultQuery2(qc2(sources, conditions, mkYields(yieldCountAll("KOUNT"))), EntityAggregates.class);
    }

    protected static ResultQuery2 qry(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions, final Yields2 yields) {
        return new ResultQuery2(qc2(sources, conditions, yields), EntityAggregates.class);
    }

    protected static SourceQuery2 srcqry(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions, final Yields2 yields) {
        return new SourceQuery2(qc2(sources, conditions, yields), EntityAggregates.class);
    }

    protected static SourceQuery2 srcqry(final IJoinNode2<? extends IJoinNode3> sources, final Yields2 yields) {
        return new SourceQuery2(qc2(sources, emptyConditions, yields), EntityAggregates.class);
    }

    protected static ResultQuery2 qry(final IJoinNode2<? extends IJoinNode3> sources, final Yields2 yields, final Class<? extends AbstractEntity<?>> resultType) {
        return new ResultQuery2(qc2(sources, emptyConditions, yields), resultType);
    }

    protected static ResultQuery2 qry(final IJoinNode2<? extends IJoinNode3> sources, final Yields2 yields, final OrderBys2 orderBys, final Class<? extends AbstractEntity<?>> resultType) {
        return new ResultQuery2(qc2(sources, emptyConditions, yields, orderBys), resultType);
    }

    protected static ResultQuery2 qry(final IJoinNode2<? extends IJoinNode3> sources, final Yields2 yields) {
        return new ResultQuery2(qc2(sources, emptyConditions, yields), EntityAggregates.class);
    }

    protected static ResultQuery2 qryCountAll(final IJoinNode2<? extends IJoinNode3> sources) {
        return new ResultQuery2(qc2(sources, emptyConditions, mkYields(yieldCountAll("KOUNT"))), EntityAggregates.class);
    }

    protected static SubQuery2 subqry(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions, final Yields2 yields, final Class<? extends AbstractEntity<?>> resultType) {
        return new SubQuery2(qc2(sources, conditions, yields), new PropType(resultType, H_LONG), false);
    }

    protected static SubQueryForExists2 typelessSubqry(final IJoinNode2<? extends IJoinNode3> sources, final Conditions2 conditions) {
        return new SubQueryForExists2(new QueryComponents2(sources, conditions, nullYields, emptyGroupBys, emptyOrderBys));
    }

    protected static SubQuery2 subqry(final IJoinNode2<? extends IJoinNode3> sources, final Yields2 yields, final PropType resultType) {
        return new SubQuery2(qc2(sources, emptyConditions, yields), resultType, false);
    }
}
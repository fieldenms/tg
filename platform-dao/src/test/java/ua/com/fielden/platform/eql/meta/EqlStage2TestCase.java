package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
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
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ExistenceTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.core.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.core.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.core.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.core.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.core.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountAll2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SubQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class EqlStage2TestCase extends EqlTestCase {

    protected static final Conditions2 emptyConditions2 = new Conditions2(false, emptyList());
    protected static final GroupBys2 emptyGroupBys2 = new GroupBys2(emptyList());
    protected static final OrderBys2 emptyOrderBys2 = new OrderBys2(emptyList());
    protected static final Yields2 emptyYields2 = new Yields2(emptyList());

    protected static AbstractPropInfo<?> pi(final Class<?> type, final String propName) {
        return DOMAIN_METADATA.lmd.getEntityInfo((Class<? extends AbstractEntity<?>>) type).getProps().get(propName);
    }

    protected static AbstractPropInfo<?> pi(final Class<?> type, final String propName, final String subPropName) {
        final AbstractPropInfo<?> propInfo = DOMAIN_METADATA.lmd.getEntityInfo((Class<? extends AbstractEntity<?>>) type).getProps().get(propName);
        if (propInfo instanceof ComponentTypePropInfo) {
            return (AbstractPropInfo<?>) ((ComponentTypePropInfo<?>) propInfo).getProps().get(subPropName);
        } else if (propInfo instanceof UnionTypePropInfo) {
            return (AbstractPropInfo<?>) ((UnionTypePropInfo<?>) propInfo).propEntityInfo.getProps().get(subPropName);
        } else {
            throw new EqlException("Can't obtain metadata for property " + propName + " and subproperty " + subPropName + " within type " + type);
        }
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qryCountAll(final ICompoundCondition0<T> unfinishedQry) {
        return qryCountAll(unfinishedQry, emptyMap());
    }

    protected static <T extends AbstractEntity<?>> T2<EntQueryGenerator, ResultQuery2> qryCountAll2(final ICompoundCondition0<T> unfinishedQry) {
        return qryCountAll2(unfinishedQry, emptyMap());
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qryCountAll(final ICompoundCondition0<T> unfinishedQry, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(DOMAIN_METADATA.lmd);
        return qb(paramValues).generateEntQueryAsResultQuery(countQry, null, null).transform(resolutionContext);
    }

    protected static <T extends AbstractEntity<?>> T2<EntQueryGenerator, ResultQuery2> qryCountAll2(final ICompoundCondition0<T> unfinishedQry, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(DOMAIN_METADATA.lmd);
        final EntQueryGenerator qb = qb(paramValues);
        return t2(qb, qb.generateEntQueryAsResultQuery(countQry, null, null).transform(resolutionContext));
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qry(final EntityResultQueryModel<T> qry) {
        return qry(qry, null);
    }

    protected static <T extends AbstractEntity<?>> ResultQuery2 qry(final EntityResultQueryModel<T> qry, final OrderingModel order) {
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(DOMAIN_METADATA.lmd);
        return qb().generateEntQueryAsResultQuery(qry, order, new EntityRetrievalModel<T>(EntityQueryUtils.fetch(qry.getResultType()), DOMAIN_METADATA_ANALYSER)).transform(resolutionContext);
    }

    protected static ResultQuery2 qry(final AggregatedResultQueryModel qry, final OrderingModel order) {
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(DOMAIN_METADATA.lmd);
        return qb().generateEntQueryAsResultQuery(qry, order, null).transform(resolutionContext);
    }

    protected static ResultQuery2 qry(final AggregatedResultQueryModel qry) {
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(DOMAIN_METADATA.lmd);
        return qb().generateEntQueryAsResultQuery(qry, null, null).transform(resolutionContext);
    }
    
    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions) {
        return new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
    }

    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields) {
        return new EntQueryBlocks2(sources, conditions, yields, emptyGroupBys2, emptyOrderBys2);
    }

    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final OrderBys2 orderBys) {
        return new EntQueryBlocks2(sources, conditions, yields, emptyGroupBys2, orderBys);
    }

    protected static Yields2 yields(final Yield2 ... yields) {
        return new Yields2(asList(yields));
    }

    protected static OrderBy2 orderDesc(final EntProp2 prop) {
        return new OrderBy2(prop, true);
    }

    protected static OrderBy2 orderDesc(final String yieldName) {
        return new OrderBy2(yieldName, true);
    }

    protected static OrderBy2 orderAsc(final EntProp2 prop) {
        return new OrderBy2(prop, false);
    }

    protected static OrderBys2 orderBys(final OrderBy2 ... orderBys) {
        return new OrderBys2(asList(orderBys));
    }

    protected static Yield2 yieldCountAll(final String alias) {
        return new Yield2(new CountAll2(), alias, false);
    }
    
    protected static Yield2 yield(final ISingleOperand2<? extends ISingleOperand3> operand, final String alias) {
        return new Yield2(operand, alias, false);
    }

    protected static EntProp2 prop(final IQrySource2<? extends IQrySource3> source, final AbstractPropInfo<?> ... propInfos) {
        return new EntProp2(source, asList(propInfos));
    }

    protected static EntProp2 propWithIsId(final IQrySource2<? extends IQrySource3> source, final AbstractPropInfo<?> ... propInfos) {
        return new EntProp2(source, asList(propInfos), true);
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
    
    protected static Sources2 sources(final IQrySource2<? extends IQrySource3> main) {
        return new Sources2(main, emptyList());
    }

    protected static Sources2 sources(final IQrySource2<? extends IQrySource3> main, final CompoundSource2... otherSources) {
        return new Sources2(main, asList(otherSources));
    }

    protected static CompoundSource2 lj(final IQrySource2<? extends IQrySource3> source, final Conditions2 conditions) {
        return new CompoundSource2(source, LJ, conditions);
    }

    protected static CompoundSource2 ij(final IQrySource2<? extends IQrySource3> source, final Conditions2 conditions) {
        return new CompoundSource2(source, IJ, conditions);
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

    protected static List<? extends ICondition2<?>> and(final ICondition2<?> ... conditions) {
        return asList(conditions);
    }

    protected static Conditions2 or(final ICondition2<?> ... conditions) {
        final List<List<? extends ICondition2<?>>> list = new ArrayList<>();
        for (final ICondition2<?> cond : conditions) {
            list.add(and(cond));
        }
        return new Conditions2(false, list);
    }

    @SafeVarargs
    protected static Conditions2 or(final List<? extends ICondition2<?>> ... conditions) {
        return new Conditions2(false, asList(conditions));
    }
    
    protected static ExistenceTest2 exists(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final Class<? extends AbstractEntity<?>> resultType) {
        return new ExistenceTest2(false, subqry(sources, conditions, yields, resultType));
    }

    protected static ExistenceTest2 notExists(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final Class<? extends AbstractEntity<?>> resultType) {
        return new ExistenceTest2(true, subqry(sources, conditions, yields, resultType));
    }

    protected static Conditions2 isNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return cond(new NullTest2(operand, false));
    }

    protected static NullTest2 isNull_(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return new NullTest2(operand, false);
    }
    
    protected static Conditions2 isNotNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return cond(new NullTest2(operand, true));
    }

    protected static NullTest2 isNotNull_(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return new NullTest2(operand, true);
    }

    protected static ComparisonTest2 eq(final EntProp2 op1, final EntProp2 op2) {
        return new ComparisonTest2(op1, EQ, op2);
    }
    
    protected static ComparisonTest2 ne(final EntProp2 op1, final EntProp2 op2) {
        return new ComparisonTest2(op1, NE, op2);
    }

    protected static EntProp1 prop(final String name) {
        return new EntProp1(name, false);
    }

    protected static QrySource2BasedOnPersistentType source(final String contextId, final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return new QrySource2BasedOnPersistentType(sourceType, DOMAIN_METADATA.lmd.getEntityInfo(sourceType), alias, contextId);
    }

    protected static QrySource2BasedOnPersistentType source(final String contextId, final Class<? extends AbstractEntity<?>> sourceType) {
        return new QrySource2BasedOnPersistentType(sourceType, DOMAIN_METADATA.lmd.getEntityInfo(sourceType), contextId);
    }
    
    protected static QrySource2BasedOnSubqueries source(final EntityInfo<?> entityInfo, final String contextId, final SourceQuery2 ... queries) {
        return new QrySource2BasedOnSubqueries(Arrays.asList(queries), null, contextId, entityInfo);
    }

    protected static ResultQuery2 qryCountAll(final Sources2 sources, final Conditions2 conditions) {
        return new ResultQuery2(qb2(sources, conditions, yields(yieldCountAll("KOUNT"))), EntityAggregates.class);
    }

    protected static ResultQuery2 qry(final Sources2 sources, final Conditions2 conditions, final Yields2 yields) {
        return new ResultQuery2(qb2(sources, conditions, yields), EntityAggregates.class);
    }

    protected static SourceQuery2 srcqry(final Sources2 sources, final Conditions2 conditions, final Yields2 yields) {
        return new SourceQuery2(qb2(sources, conditions, yields), EntityAggregates.class);
    }

    protected static SourceQuery2 srcqry(final Sources2 sources, final Yields2 yields) {
        return new SourceQuery2(qb2(sources, emptyConditions2, yields), EntityAggregates.class);
    }

    protected static ResultQuery2 qry(final Sources2 sources, final Yields2 yields , final Class<? extends AbstractEntity<?>> resultType) {
        return new ResultQuery2(qb2(sources, emptyConditions2, yields), resultType);
    }

    protected static ResultQuery2 qry(final Sources2 sources, final Yields2 yields , final OrderBys2 orderBys, final Class<? extends AbstractEntity<?>> resultType) {
        return new ResultQuery2(qb2(sources, emptyConditions2, yields, orderBys), resultType);
    }

    protected static ResultQuery2 qry(final Sources2 sources, final Yields2 yields) {
        return new ResultQuery2(qb2(sources, emptyConditions2, yields), EntityAggregates.class);
    }

    protected static ResultQuery2 qryCountAll(final Sources2 sources) {
        return new ResultQuery2(qb2(sources, emptyConditions2, yields(yieldCountAll("KOUNT"))), EntityAggregates.class);
    }

    protected static SubQuery2 subqry(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final Class<? extends AbstractEntity<?>> resultType) {
        return new SubQuery2(qb2(sources, conditions, yields), resultType);
    }
    
    protected static SubQuery2 subqry(final Sources2 sources, final Yields2 yields) {
        return new SubQuery2(qb2(sources, emptyConditions2, yields), null);
    }

}
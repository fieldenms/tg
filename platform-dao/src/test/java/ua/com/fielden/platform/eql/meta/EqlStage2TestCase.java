package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ExistenceTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountAll2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EqlStage2TestCase extends EqlTestCase {

    protected static final Conditions2 emptyConditions2 = new Conditions2(false, emptyList());
    protected static final GroupBys2 emptyGroupBys2 = new GroupBys2(emptyList());
    protected static final OrderBys2 emptyOrderBys2 = new OrderBys2(emptyList());
    protected static final Yields2 emptyYields2 = new Yields2(emptyList());

    protected static AbstractPropInfo<?> pi(final Class<?> type, final String propName) {
        return metadata.get(type).getProps().get(propName);
    }
    
    protected static <T extends AbstractEntity<?>> EntQuery2 qryCountAll(final ICompoundCondition0<T> unfinishedQry) {
        final AggregatedResultQueryModel countQry = unfinishedQry.yield().countAll().as("KOUNT").modelAsAggregate();
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(metadata);
        return qb().generateEntQueryAsResultQuery(countQry, null).transform(resolutionContext).item;
    }
    
    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions) {
        return new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
    }

    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields) {
        return new EntQueryBlocks2(sources, conditions, yields, emptyGroupBys2, emptyOrderBys2);
    }

    protected static Yields2 yields(final Yield2 ... yields) {
        return new Yields2(asList(yields));
    }
    
    protected static Yield2 yieldCountAll(final String alias) {
        return new Yield2(new CountAll2(), alias, false);
    }
    
    protected static EntProp2 prop(final IQrySource2<? extends IQrySource3> source, final AbstractPropInfo<?> ... propInfos) {
        return new EntProp2(source, asList(propInfos));
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
    
    protected static ExistenceTest2 exists(final Sources2 sources, final Conditions2 conditions, final Class<? extends AbstractEntity<?>> resultType) {
        return new ExistenceTest2(false, subqry(sources, conditions, resultType));
    }

    protected static ExistenceTest2 notExists(final Sources2 sources, final Conditions2 conditions, final Class<? extends AbstractEntity<?>> resultType) {
        return new ExistenceTest2(true, subqry(sources, conditions, resultType));
    }

    protected static NullTest2 isNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
        return new NullTest2(operand, false);
    }

    protected static NullTest2 isNotNull(final ISingleOperand2<? extends ISingleOperand3> operand) {
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
        return new QrySource2BasedOnPersistentType(sourceType, metadata.get(sourceType), alias, contextId);
    }

    protected static QrySource2BasedOnPersistentType source(final String contextId, final Class<? extends AbstractEntity<?>> sourceType) {
        return new QrySource2BasedOnPersistentType(sourceType, metadata.get(sourceType), contextId);
    }
    
    protected static EntQuery2 qryCountAll(final Sources2 sources, final Conditions2 conditions) {
        return new EntQuery2(qb2(sources, conditions, yields(yieldCountAll("KOUNT"))), EntityAggregates.class, RESULT_QUERY);
    }

    protected static EntQuery2 qryCountAll(final Sources2 sources) {
        return new EntQuery2(qb2(sources, emptyConditions2, yields(yieldCountAll("KOUNT"))), EntityAggregates.class, RESULT_QUERY);
    }

    private static EntQuery2 qry(final Sources2 sources, final Conditions2 conditions, final QueryCategory queryCategory, final Class<? extends AbstractEntity<?>> resultType) {
        return new EntQuery2(qb2(sources, conditions), resultType, queryCategory);
    }

    protected static EntQuery2 subqry(final Sources2 sources, final Conditions2 conditions, final Class<? extends AbstractEntity<?>> resultType) {
        return qry(sources, conditions, SUB_QUERY, resultType);
    }
}
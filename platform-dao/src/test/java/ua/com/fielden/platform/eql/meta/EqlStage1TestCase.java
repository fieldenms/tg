package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ComparisonTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.NullTest1;
import ua.com.fielden.platform.eql.stage1.elements.core.GroupBys1;
import ua.com.fielden.platform.eql.stage1.elements.core.OrderBys1;
import ua.com.fielden.platform.eql.stage1.elements.core.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.core.Yields1;
import ua.com.fielden.platform.eql.stage1.elements.functions.CountAll1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.elements.sources.CompoundSource1;
import ua.com.fielden.platform.eql.stage1.elements.sources.IQrySource1;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;

public class EqlStage1TestCase extends EqlTestCase {

    static int contextId = 0;
    
    protected static int nextId() {
        contextId = contextId + 1;
        return contextId;
    }

    protected static void resetId() {
        contextId = 0;
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery1 resultQry(final EntityResultQueryModel<T> qry) {
        return qb().generateEntQueryAsResultQuery(qry, null, null);
    }

    protected static ResultQuery1 resultQry(final AggregatedResultQueryModel qry) {
        return qb().generateEntQueryAsResultQuery(qry, null, null);
    }
    
    protected static EntQueryBlocks1 qb1(final Sources1 sources, final Conditions1 conditions) {
        return new EntQueryBlocks1(sources, conditions, new Yields1(emptyList()), new GroupBys1(emptyList()), new OrderBys1(emptyList()), false);
    }

    protected static EntQueryBlocks1 qb1(final Sources1 sources, final Conditions1 conditions, final Yields1 yields) {
        return new EntQueryBlocks1(sources, conditions, yields, new GroupBys1(emptyList()), new OrderBys1(emptyList()), false);
    }

    protected static Yields1 yields(final Yield1 ... yields) {
        return new Yields1(asList(yields));
    }
    
    protected static Yield1 yieldCountAll(final String alias) {
        return new Yield1(new CountAll1(), alias, false);
    }

    protected static Conditions1 conditions(final ICondition1<?> firstCondition, final CompoundCondition1... otherConditions) {
        return new Conditions1(false, firstCondition, asList(otherConditions));
    }

    protected static Conditions1 conditions(final ICondition1<?> firstCondition) {
        return new Conditions1(false, firstCondition, emptyList());
    }
    
    protected static Sources1 sources(final IQrySource1<? extends IQrySource2<?>> main) {
        return new Sources1(main, emptyList());
    }

    protected static Sources1 sources(final IQrySource1<? extends IQrySource2<?>> main, final CompoundSource1... otherSources) {
        return new Sources1(main, asList(otherSources));
    }

    protected static CompoundSource1 lj(final IQrySource1<? extends IQrySource2<?>> source, final Conditions1 conditions) {
        return new CompoundSource1(source, LJ, conditions);
    }

    protected static CompoundSource1 ij(final IQrySource1<? extends IQrySource2<?>> source, final Conditions1 conditions) {
        return new CompoundSource1(source, IJ, conditions);
    }

    protected static CompoundSource1 lj(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
        return new CompoundSource1(source, LJ, conditions(firstCondition));
    }

    protected static CompoundSource1 ij(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
        return new CompoundSource1(source, IJ, conditions(firstCondition));
    }
    
    protected static CompoundCondition1 and(final ICondition1<?> condition) {
        return new CompoundCondition1(AND, condition);
    }

    protected static CompoundCondition1 or(final ICondition1<?> condition) {
        return new CompoundCondition1(OR, condition);
    }

    protected static NullTest1 isNull(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        return new NullTest1(operand, false);
    }

    protected static NullTest1 isNotNull(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        return new NullTest1(operand, true);
    }

    protected static ComparisonTest1 eq(final EntProp1 op1, final EntProp1 op2) {
        return new ComparisonTest1(op1, EQ, op2);
    }
    
    protected static ComparisonTest1 ne(final EntProp1 op1, final EntProp1 op2) {
        return new ComparisonTest1(op1, NE, op2);
    }

    protected static EntProp1 prop(final String name) {
        return new EntProp1(name, false);
    }

    protected static EntProp1 extProp(final String name) {
        return new EntProp1(name, true);
    }

    protected static QrySource1BasedOnPersistentType source(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return new QrySource1BasedOnPersistentType(sourceType, alias, nextId());
    }

    protected static QrySource1BasedOnPersistentType source(final Class<? extends AbstractEntity<?>> sourceType) {
        return new QrySource1BasedOnPersistentType(sourceType, null, nextId());
    }

    protected static Sources1 sources(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return sources(source(sourceType, alias));
    }

    protected static Sources1 sources(final Class<? extends AbstractEntity<?>> sourceType) {
        return sources(source(sourceType));
    }
}
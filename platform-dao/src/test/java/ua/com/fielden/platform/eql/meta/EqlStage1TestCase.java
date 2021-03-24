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
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.conditions.ComparisonTest1;
import ua.com.fielden.platform.eql.stage1.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.conditions.NullTest1;
import ua.com.fielden.platform.eql.stage1.etc.GroupBys1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.etc.Yield1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.functions.CountAll1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.sources.CompoundSource1;
import ua.com.fielden.platform.eql.stage1.sources.ISource1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public class EqlStage1TestCase extends EqlTestCase {

    static int sourceId = 0;
    
    protected static int nextSourceId() {
        sourceId = sourceId + 1;
        return sourceId;
    }

    protected static void resetId() {
        sourceId = 0;
    }
    
    protected static <T extends AbstractEntity<?>> ResultQuery1 resultQry(final EntityResultQueryModel<T> qry) {
        return qb().generateAsResultQuery(qry, null, null);
    }

    protected static ResultQuery1 resultQry(final AggregatedResultQueryModel qry) {
        return qb().generateAsResultQuery(qry, null, null);
    }
    
    protected static QueryBlocks1 qb1(final Sources1 sources, final Conditions1 conditions) {
        return new QueryBlocks1(sources, conditions, new Yields1(emptyList()), new GroupBys1(emptyList()), new OrderBys1(emptyList()), false);
    }

    protected static QueryBlocks1 qb1(final Sources1 sources, final Conditions1 conditions, final Yields1 yields) {
        return new QueryBlocks1(sources, conditions, yields, new GroupBys1(emptyList()), new OrderBys1(emptyList()), false);
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
    
    protected static Sources1 sources(final ISource1<? extends ISource2<?>> main) {
        return new Sources1(main, emptyList());
    }

    protected static Sources1 sources(final ISource1<? extends ISource2<?>> main, final CompoundSource1... otherSources) {
        return new Sources1(main, asList(otherSources));
    }

    protected static CompoundSource1 lj(final ISource1<? extends ISource2<?>> source, final Conditions1 conditions) {
        return new CompoundSource1(source, LJ, conditions);
    }

    protected static CompoundSource1 ij(final ISource1<? extends ISource2<?>> source, final Conditions1 conditions) {
        return new CompoundSource1(source, IJ, conditions);
    }

    protected static CompoundSource1 lj(final ISource1<? extends ISource2<?>> source, final ICondition1<?> firstCondition) {
        return new CompoundSource1(source, LJ, conditions(firstCondition));
    }

    protected static CompoundSource1 ij(final ISource1<? extends ISource2<?>> source, final ICondition1<?> firstCondition) {
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

    protected static ComparisonTest1 eq(final Prop1 op1, final Prop1 op2) {
        return new ComparisonTest1(op1, EQ, op2);
    }
    
    protected static ComparisonTest1 ne(final Prop1 op1, final Prop1 op2) {
        return new ComparisonTest1(op1, NE, op2);
    }

    protected static Prop1 prop(final String name) {
        return new Prop1(name, false);
    }

    protected static Prop1 extProp(final String name) {
        return new Prop1(name, true);
    }

    protected static Source1BasedOnPersistentType source(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return new Source1BasedOnPersistentType(sourceType, alias, nextSourceId());
    }

    protected static Source1BasedOnPersistentType source(final Class<? extends AbstractEntity<?>> sourceType) {
        return new Source1BasedOnPersistentType(sourceType, null, nextSourceId());
    }

    protected static Sources1 sources(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return sources(source(sourceType, alias));
    }

    protected static Sources1 sources(final Class<? extends AbstractEntity<?>> sourceType) {
        return sources(source(sourceType));
    }
}
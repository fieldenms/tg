package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;
import static ua.com.fielden.platform.eql.stage1.etc.GroupBys1.emptyGroupBys;
import static ua.com.fielden.platform.eql.stage1.etc.OrderBys1.emptyOrderBys;
import static ua.com.fielden.platform.eql.stage1.etc.Yields1.emptyYields;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.conditions.ComparisonTest1;
import ua.com.fielden.platform.eql.stage1.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.conditions.NullTest1;
import ua.com.fielden.platform.eql.stage1.etc.Yield1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CountAll1;
import ua.com.fielden.platform.eql.stage1.sources.ISource1;
import ua.com.fielden.platform.eql.stage1.sources.ISources1;
import ua.com.fielden.platform.eql.stage1.sources.SingleNodeSources1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;

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
    
    protected static QueryBlocks1 qb1(final ISources1<? extends ISources2<?>> sources, final Conditions1 conditions) {
        return new QueryBlocks1(sources, conditions, emptyYields, emptyGroupBys, emptyOrderBys, false);
    }

    protected static QueryBlocks1 qb1(final ISources1<? extends ISources2<?>> sources, final Conditions1 conditions, final Yields1 yields) {
        return new QueryBlocks1(sources, conditions, yields, emptyGroupBys, emptyOrderBys, false);
    }

    protected static Yields1 yields(final Yield1 ... yields) {
        if (yields.length > 0) {
            return new Yields1(asList(yields)); 
        } else {
            return emptyYields;
        }
    }
    
    protected static Yield1 yieldCountAll(final String alias) {
        return new Yield1(CountAll1.INSTANCE, alias, false);
    }

    protected static Conditions1 conditions(final ICondition1<?> firstCondition, final CompoundCondition1... otherConditions) {
        return new Conditions1(false, firstCondition, asList(otherConditions));
    }

    protected static Conditions1 conditions(final ICondition1<?> firstCondition) {
        return new Conditions1(false, firstCondition, emptyList());
    }
    
    protected static ISources1<? extends ISources2<?>> sources(final ISource1<? extends ISource2<?>> main) {
        return new SingleNodeSources1(main);
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

    protected static ISources1<? extends ISources2<?>> sources(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return sources(source(sourceType, alias));
    }

    protected static ISources1<? extends ISources2<?>> sources(final Class<? extends AbstractEntity<?>> sourceType) {
        return sources(source(sourceType));
    }
}
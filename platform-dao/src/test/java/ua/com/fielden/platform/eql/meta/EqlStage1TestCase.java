package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;
import static ua.com.fielden.platform.eql.stage1.conditions.Conditions1.EMPTY_CONDITIONS;
import static ua.com.fielden.platform.eql.stage1.sundries.GroupBys1.EMPTY_GROUP_BYS;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.EMPTY_ORDER_BYS;
import static ua.com.fielden.platform.eql.stage1.sundries.Yields1.EMPTY_YIELDS;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.conditions.ComparisonPredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.conditions.NullPredicate1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CountAll1;
import ua.com.fielden.platform.eql.stage1.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sources.ISource1;
import ua.com.fielden.platform.eql.stage1.sources.JoinLeafNode1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.sundries.Yield1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public abstract class EqlStage1TestCase extends EqlTestCase {

    static int sourceId = 0;

    protected static int nextSourceId() {
        sourceId = sourceId + 1;
        return sourceId;
    }

    protected static void resetId() {
        sourceId = 0;
    }

    @Before
    public void setUp() {
        resetId();
    }

    protected static <T extends AbstractEntity<?>> ResultQuery1 resultQry(final EntityResultQueryModel<T> qry) {
        return qb().generateAsResultQuery(qry, null, null);
    }

    protected static <T extends AbstractEntity<?>> ResultQuery1 resultQry(final EntityResultQueryModel<T> qry, final Map<String, Object> paramValues) {
        return qb(paramValues).generateAsResultQuery(qry, null, null);
    }

    protected static ResultQuery1 resultQry(final AggregatedResultQueryModel qry) {
        return qb().generateAsResultQuery(qry, null, null);
    }

    protected static QueryComponents1 qc1(final IJoinNode1<? extends IJoinNode2<?>> sources, final Conditions1 conditions) {
        return new QueryComponents1(Optional.ofNullable(sources), conditions, EMPTY_CONDITIONS, EMPTY_YIELDS, EMPTY_GROUP_BYS, EMPTY_ORDER_BYS, false, false);
    }

    protected static QueryComponents1 qc1(final IJoinNode1<? extends IJoinNode2<?>> sources, final Conditions1 conditions, final Yields1 yields) {
        return new QueryComponents1(Optional.ofNullable(sources), conditions, EMPTY_CONDITIONS, yields, EMPTY_GROUP_BYS, EMPTY_ORDER_BYS, false, false);
    }

    protected static Yields1 yields(final Yield1 ... yields) {
        if (yields.length > 0) {
            return Yields1.yields(asList(yields));
        } else {
            return EMPTY_YIELDS;
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

    protected static IJoinNode1<? extends IJoinNode2<?>> sources(final ISource1<? extends ISource2<?>> main) {
        return new JoinLeafNode1(main);
    }

    protected static CompoundCondition1 and(final ICondition1<?> condition) {
        return new CompoundCondition1(AND, condition);
    }

    protected static CompoundCondition1 or(final ICondition1<?> condition) {
        return new CompoundCondition1(OR, condition);
    }

    protected static NullPredicate1 isNull(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        return new NullPredicate1(operand, false);
    }

    protected static NullPredicate1 isNotNull(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        return new NullPredicate1(operand, true);
    }

    protected static ComparisonPredicate1 eq(final Prop1 op1, final Prop1 op2) {
        return new ComparisonPredicate1(op1, EQ, op2);
    }

    protected static ComparisonPredicate1 ne(final Prop1 op1, final Prop1 op2) {
        return new ComparisonPredicate1(op1, NE, op2);
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

    protected static IJoinNode1<? extends IJoinNode2<?>> sources(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return sources(source(sourceType, alias));
    }

    protected static IJoinNode1<? extends IJoinNode2<?>> sources(final Class<? extends AbstractEntity<?>> sourceType) {
        return sources(source(sourceType));
    }
}

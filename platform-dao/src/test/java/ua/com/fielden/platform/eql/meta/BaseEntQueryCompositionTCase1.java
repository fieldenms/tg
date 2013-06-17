package ua.com.fielden.platform.eql.meta;

import java.util.Arrays;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s1.elements.GroupBys1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.Sources1;
import ua.com.fielden.platform.eql.s1.elements.Yields1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import static org.junit.Assert.assertEquals;

public class BaseEntQueryCompositionTCase1 extends BaseEntQueryTCase1 {
    protected final String mercLike = "MERC%";
    protected final String merc = "MERC";
    protected final String audi = "AUDI";

    protected final static ComparisonOperator _eq = ComparisonOperator.EQ;
    protected final static ComparisonOperator _gt = ComparisonOperator.GT;
    protected final static ComparisonOperator _lt = ComparisonOperator.LT;
    protected final static LogicalOperator _and = LogicalOperator.AND;
    protected final static LogicalOperator _or = LogicalOperator.OR;
    protected final static ArithmeticalOperator _mult = ArithmeticalOperator.MULT;
    protected final static ArithmeticalOperator _div = ArithmeticalOperator.DIV;
    protected final static ArithmeticalOperator _add = ArithmeticalOperator.ADD;
    protected final static ArithmeticalOperator _sub = ArithmeticalOperator.SUB;

    protected static Conditions1 group(final boolean negation, final ICondition1<? extends ICondition2> firstCondition, final CompoundCondition1... otherConditions) {
	return new Conditions1(negation, firstCondition, Arrays.asList(otherConditions));
    }

    protected static CompoundCondition1 compound(final LogicalOperator operator, final ICondition1<? extends ICondition2> condition) {
	return new CompoundCondition1(operator, condition);
    }

    protected static void assertModelsEquals(final Conditions1 exp, final Conditions1 act) {
	assertEquals(("Condition models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final Yields1 exp, final Yields1 act) {
	assertEquals(("Yields models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final Sources1 exp, final Sources1 act) {
	assertEquals(("Sources models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final GroupBys1 exp, final GroupBys1 act) {
	assertEquals(("Groups models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static Conditions1 conditions(final ICompoundCondition0 condition) {
	return entResultQry(condition.model()).getConditions();
    }

    protected static Conditions1 conditions(final ICompoundCondition0 condition, final Map<String, Object> paramValues) {
	return entResultQry(condition.model(), paramValues).getConditions();
    }

    protected static Conditions1 conditions(final ICondition1<? extends ICondition2> firstCondition, final CompoundCondition1... otherConditions) {
	return new Conditions1(false, firstCondition, Arrays.asList(otherConditions));
    }

    protected static Expression1 expression(final ISingleOperand1<? extends ISingleOperand2> first, final CompoundSingleOperand1 ... others) {
	return new Expression1(first, Arrays.asList(others));
    }

    protected static CompoundSingleOperand1 compound(final ArithmeticalOperator operator, final ISingleOperand1<? extends ISingleOperand2> operand) {
	return new CompoundSingleOperand1(operand, operator);
    }
}
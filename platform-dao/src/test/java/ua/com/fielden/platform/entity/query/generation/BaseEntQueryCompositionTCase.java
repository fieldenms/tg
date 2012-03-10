package ua.com.fielden.platform.entity.query.generation;

import java.util.Arrays;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.Conditions;
import ua.com.fielden.platform.entity.query.generation.elements.Sources;
import ua.com.fielden.platform.entity.query.generation.elements.Expression;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditions;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBys;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import static org.junit.Assert.assertEquals;

public class BaseEntQueryCompositionTCase extends BaseEntQueryTCase {
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

    protected static GroupedConditions group(final boolean negation, final ICondition firstCondition, final CompoundCondition... otherConditions) {
	return new GroupedConditions(negation, firstCondition, Arrays.asList(otherConditions));
    }

    protected static CompoundCondition compound(final LogicalOperator operator, final ICondition condition) {
	return new CompoundCondition(operator, condition);
    }

    protected static Conditions conditions(final ICompoundCondition0 condition) {
	return entResultQry(condition.model()).getConditions();
    }

    protected static void assertModelsEquals(final Conditions exp, final Conditions act) {
	assertEquals(("Condition models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final Yields exp, final Yields act) {
	assertEquals(("Yields models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final Sources exp, final Sources act) {
	assertEquals(("Sources models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final GroupBys exp, final GroupBys act) {
	assertEquals(("Groups models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static Conditions conditions(final ICompoundCondition0 condition, final Map<String, Object> paramValues) {
	return entResultQry(condition.model(), paramValues).getConditions();
    }

    protected static Conditions conditions(final ICondition firstCondition, final CompoundCondition... otherConditions) {
	return new Conditions(firstCondition, Arrays.asList(otherConditions));
    }

    protected static Expression expression(final ISingleOperand first, final CompoundSingleOperand ... others) {
	return new Expression(first, Arrays.asList(others));
    }

    protected static CompoundSingleOperand compound(final ArithmeticalOperator operator, final ISingleOperand operand) {
	return new CompoundSingleOperand(operand, operator);
    }
}
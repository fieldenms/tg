package ua.com.fielden.platform.entity.query.generation;

import java.util.Arrays;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.SourcesModel;
import ua.com.fielden.platform.entity.query.generation.elements.Expression;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.YieldsModel;
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

    protected static GroupedConditionsModel group(final boolean negation, final ICondition firstCondition, final CompoundConditionModel... otherConditions) {
	return new GroupedConditionsModel(negation, firstCondition, Arrays.asList(otherConditions));
    }

    protected static CompoundConditionModel compound(final LogicalOperator operator, final ICondition condition) {
	return new CompoundConditionModel(operator, condition);
    }

    protected static ConditionsModel conditions(final ICompoundCondition0 condition) {
	return entResultQry(condition.model()).getConditions();
    }

    protected static void assertModelsEquals(final ConditionsModel exp, final ConditionsModel act) {
	assertEquals(("Condition models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final YieldsModel exp, final YieldsModel act) {
	assertEquals(("Yields models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final SourcesModel exp, final SourcesModel act) {
	assertEquals(("Sources models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static void assertModelsEquals(final GroupsModel exp, final GroupsModel act) {
	assertEquals(("Groups models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    protected static ConditionsModel conditions(final ICompoundCondition0 condition, final Map<String, Object> paramValues) {
	return entResultQry(condition.model(), paramValues).getConditions();
    }

    protected static ConditionsModel conditions(final ICondition firstCondition, final CompoundConditionModel... otherConditions) {
	return new ConditionsModel(firstCondition, Arrays.asList(otherConditions));
    }

    protected static Expression expression(final ISingleOperand first, final CompoundSingleOperand ... others) {
	return new Expression(first, Arrays.asList(others));
    }

    protected static CompoundSingleOperand compound(final ArithmeticalOperator operator, final ISingleOperand operand) {
	return new CompoundSingleOperand(operand, operator);
    }
}
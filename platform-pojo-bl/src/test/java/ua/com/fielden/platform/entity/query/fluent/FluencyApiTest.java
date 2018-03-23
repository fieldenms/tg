package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.sample.domain.TgVehicle;

public class FluencyApiTest extends TestCase {

	private static final List<Method> objectMethods = new ArrayList<>();
    private static final List<String> additionalMethodNames = new ArrayList<>();

    static {
        for (final Method method : Object.class.getMethods()) {
            objectMethods.add(method);
        }
        
        additionalMethodNames.add("toString");
        additionalMethodNames.add("equals");
        additionalMethodNames.add("hashCode");
        additionalMethodNames.add("getTokens");
        additionalMethodNames.add("setTokens");
    }

	private static final String[] functions = new String[] { "round", "now", "caseWhen", "lowerCase", "upperCase", "ifNull",
			"dateOf", "hourOf", "dayOf", "monthOf", "yearOf", "minuteOf", "secondOf", "count", "concat", "absOf", "addTimeIntervalOf", "dayOfWeekOf" };

	private static final String[] aggregateFunctions = new String[] { "maxOf", "minOf", "sumOf", "countOf", "avgOf",
			"sumOfDistinct", "countOfDistinct", "avgOfDistinct", "countAll" };

	private static final String[] anyOfs = new String[] { "anyOfProps", "anyOfModels", "anyOfValues", "anyOfParams",
			"anyOfIParams", "anyOfExpressions" };

	private static final String[] allOfs = new String[] { "allOfProps", "allOfModels", "allOfValues", "allOfParams",
			"allOfIParams", "allOfExpressions" };

	private static final String[] exists = new String[] { "exists", "notExists", "existsAnyOf", "notExistsAnyOf",
			"existsAllOf", "notExistsAllOf" };

	private static final String model = "model";
	private static final String modelAsAggregate = "modelAsAggregate";
	private static final String prop = "prop";
	private static final String extProp = "extProp";
	private static final String param = "param";
	private static final String iParam = "iParam";
	private static final String val = "val";
	private static final String iVal = "iVal";
	private static final String begin = "begin";
	private static final String notBegin = "notBegin";
	private static final String beginExpr = "beginExpr";
	private static final String expr = "expr";
	private static final String condition = "condition";
	private static final String negatedCondition = "negatedCondition";
	private static final String join = "join";
	private static final String as = "as";
	private static final String asRequired = "asRequired";
	private static final String modelAsEntity = "modelAsEntity";
	private static final String modelAsPrimitive = "modelAsPrimitive";
	private static final String where = "where";
	private static final String groupBy = "groupBy";
	private static final String yield = "yield";
	private static final String yieldAll = "yieldAll";
	private static final String leftJoin = "leftJoin";

	private static Set<String> getMethods(final Class<?> type) {
		final Set<String> result = new HashSet<>();
		for (final Method method : type.getMethods()) {
			if (!objectMethods.contains(method) && !additionalMethodNames.contains(method.getName())) {
				result.add(method.getName());
			}
		}
		return result;
	}

	private static String[] array(final String... strings) {
		return strings;
	}

	private static String compare(final Set<String> set, final String[]... methods) {
		final List<String> methodList = new ArrayList<String>();
		for (final String[] method : methods) {
			methodList.addAll(Arrays.asList(method));
		}
		final List<String> missingMethods = new ArrayList<String>();

		for (final String correctMethod : set) {
			if (!methodList.contains(correctMethod)) {
				missingMethods.add(correctMethod);
			}
		}

		final List<String> obsoleteMethods = new ArrayList<String>();

		for (final String providedMethod : methodList) {
			if (!set.contains(providedMethod)) {
				obsoleteMethods.add(providedMethod);
			}
		}

		return (missingMethods.size() + obsoleteMethods.size()) == 0 ? null
				: ((missingMethods.size() > 0 ? "Missing: " + missingMethods : "")
						+ (obsoleteMethods.size() > 0 ? (" Obsolete: " + obsoleteMethods) : ""));
	}

	public static void checkFluency(final Object queryInterface, final String[]... methods) {
		final String result = compare(getMethods(queryInterface.getClass()), methods);
		assertNull(result, result);
	}

	public void test_IFromAlias() {
		checkFluency( //
				select(TgVehicle.class), //
				array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, yieldAll, as, join, leftJoin));
	}

	@SuppressWarnings("unchecked")
	public void test_IFromAlias2() {
		checkFluency( //
				select(select(TgVehicle.class).model()), //
				array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, yieldAll, as, join, leftJoin));
	}

	public void test_IFromAlias3() {
		checkFluency( //
				select(select(TgVehicle.class).yield().countAll().as("count").modelAsAggregate()), //
				array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, yieldAll, as, join, leftJoin));
	}

	public void test_IWhere0() {
		checkFluency( //
				select(TgVehicle.class).where(), //
				array(condition, negatedCondition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
						beginExpr, expr),
				exists, functions, anyOfs, allOfs);
	}

	public void test_IWhere1() {
		checkFluency( //
				select(TgVehicle.class).where().begin(), //
				array(condition, negatedCondition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
						beginExpr, expr),
				exists, functions, anyOfs, allOfs);
	}

	public void test_IWhere2() {
		checkFluency( //
				select(TgVehicle.class).where().begin().begin(), //
				array(condition, negatedCondition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
						beginExpr, expr),
				exists, functions, anyOfs, allOfs);
	}

	public void test_IWhere3() {
		checkFluency( //
				select(TgVehicle.class).where().begin().begin().begin(), //
				array(/* condition, */model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), exists,
				functions, anyOfs, allOfs);
	}

	public void test_IFunctionLastArgument_with_ICompleted() {
		checkFluency( //
				select(TgVehicle.class).groupBy(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
	}

	public void test_IFunctionYieldedLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
	}

	public void test_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().prop("prop"), //
				array(as, asRequired, modelAsEntity, modelAsPrimitive));
	}

	public void test_IFunctionLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().secondOf(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
	}

	public void test_IFunctionLastArgument_with_IComparisonOperator0() {
		checkFluency( //
				select(TgVehicle.class).where().secondOf(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
	}

	public void test_IYieldExprItem0_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().beginExpr(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
	}

	public void test_IYieldExprItem1_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().beginExpr().beginExpr(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
	}

	public void test_IYieldExprItem2_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr(), //
				array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
	}

	public void test_IYieldExprItem3_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
		checkFluency( //
				select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr().beginExpr(), //
				array(model, prop, extProp, param, iParam, val, iVal, expr), functions, aggregateFunctions);
	}

	public void previously_composed_query_stmt_remains_immutable_when_the_next_query_operator_has_been_added() {
		final ICompoundCondition0<TgVehicle> qryStmt = select(TgVehicle.class).as("veh").where().prop("veh.model").isNull();
		final ICompoundCondition0<TgVehicle> equalQryStmt = select(TgVehicle.class).as("veh").where().prop("veh.model").isNull();

		assertTrue(qryStmt.equals(equalQryStmt));
		
		equalQryStmt.groupBy().prop("veh.model");
		
		assertTrue(qryStmt.equals(equalQryStmt));
	}
}
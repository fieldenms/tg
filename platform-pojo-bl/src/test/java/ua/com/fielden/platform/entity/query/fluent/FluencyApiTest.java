package ua.com.fielden.platform.entity.query.fluent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


public class FluencyApiTest extends TestCase {

    private static List<Method> objectMethods = new ArrayList<Method>();

    {
	for (final Method method : Object.class.getMethods()) {
	    objectMethods.add(method);
	}
    }

    private static String[] functions = new String[]{"round", "now", "caseWhen", "lowerCase", "upperCase", "ifNull", "hourOf", "dayOf", "monthOf", "yearOf", "minuteOf", "secondOf", "count", "concat"};

    private static String[] aggregateFunctions = new String[]{"maxOf", "minOf", "sumOf", "countOf", "avgOf", "sumOfDistinct", "countOfDistinct", "avgOfDistinct", "countAll"};

    private static String[] anyOfs = new String[]{"anyOfProps", "anyOfModels", "anyOfValues", "anyOfParams", "anyOfIParams", "anyOfExpressions"};

    private static String[] allOfs = new String[]{"allOfProps", "allOfModels", "allOfValues", "allOfParams", "allOfIParams", "allOfExpressions"};

    private static String[] exists = new String[]{"exists", "notExists", "existsAnyOf", "notExistsAnyOf", "existsAllOf", "notExistsAllOf"};

    private static String model = "model";
    private static String modelAsAggregate = "modelAsAggregate";
    private static String prop = "prop";
    private static String extProp = "extProp";
    private static String param = "param";
    private static String iParam = "iParam";
    private static String val = "val";
    private static String iVal = "iVal";
    private static String begin = "begin";
    private static String notBegin = "notBegin";
    private static String beginExpr = "beginExpr";
    private static String expr = "expr";
    private static String condition = "condition";
    private static String join = "join";
    private static String as = "as";
    private static String modelAsEntity = "modelAsEntity";
    private static String modelAsPrimitive = "modelAsPrimitive";
    private static String where = "where";
    private static String groupBy = "groupBy";
    private static String yield = "yield";
    private static String leftJoin = "leftJoin";

    private static Set<String> getMethods(final Class<?> type) {
	final Set<String> result = new HashSet<String>();
	for (final Method method : type.getMethods()) {
	    if (!objectMethods.contains(method) && !"toString".equals(method.getName()) && !"getTokens".equals(method.getName()) && !"setTokens".equals(method.getName())) {
		result.add(method.getName());
	    }
	}
	return result;
    }

    private static String[] array(final String ... strings) {
	return strings;
    }

    private static String compare(final Set<String> set, final String[]...methods) {
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

	return (missingMethods.size() + obsoleteMethods.size()) == 0 ? null : ((missingMethods.size() > 0 ? "Missing: " + missingMethods : "") + (obsoleteMethods.size() > 0 ? (" Obsolete: " + obsoleteMethods) : ""));
    }

    public static void checkFluency(final Object queryInterface, final String[]...methods) {
	final String result = compare(getMethods(queryInterface.getClass()), methods);
	assertNull(result, result);
    }

    public void test_IFromAlias(){
	checkFluency( //
		select(TgVehicle.class), //
		array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, as, join, leftJoin));
    }

    @SuppressWarnings("unchecked")
    public void test_IFromAlias2(){
	checkFluency( //
		select(select(TgVehicle.class).model()), //
		array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, as, join, leftJoin));
    }

    public void test_IFromAlias3(){
	checkFluency( //
		select(select(TgVehicle.class).yield().countAll().as("count").modelAsAggregate()), //
		array(model, modelAsAggregate, modelAsEntity, where, groupBy, yield, as, join, leftJoin));
    }

    public void test_IWhere0(){
	checkFluency( //
		select(TgVehicle.class).where(), //
		array(condition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin, beginExpr, expr), exists, functions, anyOfs, allOfs);
    }

    public void test_IWhere1(){
	checkFluency( //
		select(TgVehicle.class).where().begin(), //
		array(condition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin, beginExpr, expr), exists, functions, anyOfs, allOfs);
    }

    public void test_IWhere2(){
	checkFluency( //
		select(TgVehicle.class).where().begin().begin(), //
		array(condition, model, prop, extProp, param, iParam, val, iVal, begin, notBegin, beginExpr, expr), exists, functions, anyOfs, allOfs);
    }

    public void test_IWhere3(){
	checkFluency( //
		select(TgVehicle.class).where().begin().begin().begin(), //
		array(/*condition, */model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), exists, functions, anyOfs, allOfs);
    }

    public void test_IFunctionLastArgument_with_ICompleted(){
	checkFluency( //
		select(TgVehicle.class).groupBy(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    public void test_IFunctionYieldedLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr, join), functions, aggregateFunctions);
    }

    public void test_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
	checkFluency( //
		select(TgVehicle.class).yield().prop("prop"), //
		array(as, modelAsEntity, modelAsPrimitive));
    }

    public void test_IFunctionLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield().secondOf(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    public void test_IFunctionLastArgument_with_IComparisonOperator0(){
	checkFluency( //
		select(TgVehicle.class).where().secondOf(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    public void test_IYieldExprItem0_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield().beginExpr(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr, join), functions, aggregateFunctions);
    }

    public void test_IYieldExprItem1_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield().beginExpr().beginExpr(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr, join), functions, aggregateFunctions);
    }

    public void test_IYieldExprItem2_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr(), //
		array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr, join), functions, aggregateFunctions);
    }

    public void test_IYieldExprItem3_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded(){
	checkFluency( //
		select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr().beginExpr(), //
		array(model, prop, extProp, param, iParam, val, iVal, expr, join), functions, aggregateFunctions);
    }
}
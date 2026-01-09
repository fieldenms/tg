package ua.com.fielden.platform.entity.query.fluent;

import org.junit.Assert;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * This test suite verifies properties of the EQL fluent API.
 * It does not cover all possible permutations at this stage, but should be enhanced gradually.
 */
public class FluencyApiTest {

    private static final Set<Method> objectMethods = Set.of(Object.class.getMethods());
    private static final Set<String> additionalMethodNames =
            Arrays.stream(AbstractQueryLink.class.getDeclaredMethods()).map(Method::getName).collect(toSet());

    private static final String[] functions = new String[] { "round", "now", "caseWhen", "lowerCase", "upperCase", "ifNull",
            "dateOf", "hourOf", "dayOf", "monthOf", "yearOf", "minuteOf", "secondOf", "count", "concat", "absOf", "addTimeIntervalOf", "dayOfWeekOf" };

    private static final String[] aggregateFunctions = new String[] { "maxOf", "minOf", "sumOf", "countOf", "avgOf", "concatOf",
            "sumOfDistinct", "countOfDistinct", "avgOfDistinct", "countAll" };

    private static final String[] anyOfs = new String[] { "anyOfProps", "anyOfModels", "anyOfValues", "anyOfParams",
            "anyOfIParams", "anyOfExpressions" };

    private static final String[] allOfs = new String[] { "allOfProps", "allOfModels", "allOfValues", "allOfParams",
            "allOfIParams", "allOfExpressions" };

    private static final String[] singleOperatorConditions = new String[] { "exists", "notExists", "existsAnyOf", "notExistsAnyOf",
            "existsAllOf", "notExistsAllOf", "critCondition", "condition", "negatedCondition"};

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
    private static final String join = "join";
    private static final String as = "as";
    private static final String asRequired = "asRequired";
    private static final String modelAsEntity = "modelAsEntity";
    private static final String modelAsPrimitive = "modelAsPrimitive";
    private static final String where = "where";
    private static final String groupBy = "groupBy";
    private static final String orderBy = "orderBy";
    private static final String yield = "yield";
    private static final String yieldAll = "yieldAll";
    private static final String leftJoin = "leftJoin";
    private static final String order = "order";
    private static final String asc = "asc";
    private static final String desc = "desc";
    private static final String limit = "limit";
    private static final String offset = "offset";

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
        Assert.assertNull(result, result);
    }

    /**
     * This method is provided to address a limitation of the current approach for testing progressive completions.
     * The limitation applies when a fluent interface implementation is broader than the interface, i.e., implements other fluent interfaces.
     * This results in {@link #getMethods(Class)} picking up additional methods, failing the asserted expectations.
     * This method addresses this by accepting an expected contract type {@code queryInterfaceType} for the partial query instance.
     * <p>
     * However, it should be preferred to implement fluent interfaces with high granularity to avoid the need for using this method.
     */
    public static void checkFluency(final Object queryInterface, final Class<?> queryInterfaceType, final String[]... methods) {
        assertTrue("Query interface does not match expected type.", queryInterfaceType.isAssignableFrom(queryInterface.getClass()));
        final String result = compare(getMethods(queryInterfaceType), methods);
        Assert.assertNull(result, result);
    }

    @Test
    public void test_IFromAlias() {
        checkFluency( //
                select(TgVehicle.class), //
                array(model, modelAsAggregate, modelAsEntity, where, groupBy, orderBy, yield, yieldAll, as, join, leftJoin));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_IFromAlias2() {
        checkFluency( //
                select(select(TgVehicle.class).model()), //
                array(model, modelAsAggregate, modelAsEntity, where, groupBy, orderBy, yield, yieldAll, as, join, leftJoin));
    }

    @Test
    public void test_IFromAlias3() {
        checkFluency( //
                select(select(TgVehicle.class).yield().countAll().as("count").modelAsAggregate()), //
                array(model, modelAsAggregate, modelAsEntity, where, groupBy, orderBy, yield, yieldAll, as, join, leftJoin));
    }

    @Test
    public void test_IWhere0() {
        checkFluency( //
                select(TgVehicle.class).where(), //
                array(model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
                        beginExpr, expr),
                singleOperatorConditions, functions, anyOfs, allOfs);
    }

    @Test
    public void test_IWhere1() {
        checkFluency( //
                select(TgVehicle.class).where().begin(), //
                array(model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
                        beginExpr, expr),
                singleOperatorConditions, functions, anyOfs, allOfs);
    }

    @Test
    public void test_IWhere2() {
        checkFluency( //
                select(TgVehicle.class).where().begin().begin(), //
                array(model, prop, extProp, param, iParam, val, iVal, begin, notBegin,
                        beginExpr, expr),
                singleOperatorConditions, functions, anyOfs, allOfs);
    }

    @Test
    public void test_IWhere3() {
        checkFluency( //
                select(TgVehicle.class).where().begin().begin().begin(), //
                array(/* condition, */model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), singleOperatorConditions,
                functions, anyOfs, allOfs);
    }

    @Test
    public void test_IFunctionLastArgument_with_ICompleted() {
        checkFluency( //
                select(TgVehicle.class).groupBy(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    @Test
    public void test_IFunctionYieldedLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
    }

    @Test
    public void test_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().prop("prop"), //
                array(as, asRequired, modelAsEntity, modelAsPrimitive));
    }

    @Test
    public void test_IFunctionLastArgument_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().secondOf(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    @Test
    public void test_IFunctionLastArgument_with_IComparisonOperator0() {
        checkFluency( //
                select(TgVehicle.class).where().secondOf(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions);
    }

    @Test
    public void test_IYieldExprItem0_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().beginExpr(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
    }

    @Test
    public void test_IYieldExprItem1_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().beginExpr().beginExpr(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
    }

    @Test
    public void test_IYieldExprItem2_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr(), //
                array(model, prop, extProp, param, iParam, val, iVal, beginExpr, expr), functions, aggregateFunctions);
    }

    @Test
    public void test_IYieldExprItem3_with_IFirstYieldedItemAlias_with_ISubsequentCompletedAndYielded() {
        checkFluency( //
                select(TgVehicle.class).yield().beginExpr().beginExpr().beginExpr().beginExpr(), //
                array(model, prop, extProp, param, iParam, val, iVal, expr), functions, aggregateFunctions);
    }

    @Test
    public void intermediate_query_instances_are_immutable() {
        final var qryStmt = select(TgVehicle.class).as("veh").where().prop("veh.model").isNull();
        final var equalQryStmt = select(TgVehicle.class).as("veh").where().prop("veh.model").isNull();
        assertEquals(qryStmt, equalQryStmt);

        equalQryStmt.groupBy().prop("veh.model");

        assertEquals(qryStmt, equalQryStmt);
    }

    @Test
    public void orderBy_progressive_completions() {
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy(),
                array(order, model, prop, extProp, param, iParam, val, iVal, expr, yield, beginExpr), functions);
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy().prop("initDate"),
                array(asc, desc));
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy().prop("initDate").asc(),
                array(order, limit, offset, model, prop, extProp, param, iParam, val, iVal, expr, beginExpr, yield, yieldAll, modelAsEntity, modelAsAggregate), functions);
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy().prop("initDate").asc().limit(1),
                array(offset, yield, yieldAll, model, modelAsEntity, modelAsAggregate));
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy().prop("initDate").asc().limit(1).offset(1),
                array(yield, yieldAll, model, modelAsEntity, modelAsAggregate));
        checkFluency(
                select(TgVehicle.class).as("veh").orderBy().prop("initDate").asc().offset(1),
                array(yield, yieldAll, model, modelAsEntity, modelAsAggregate));
    }

}

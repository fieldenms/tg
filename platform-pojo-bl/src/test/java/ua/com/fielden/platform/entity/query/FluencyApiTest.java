package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;


public class FluencyApiTest extends TestCase {

    private static List<Method> objectMethods = new ArrayList<Method>();

    {
	for (final Method method : Object.class.getMethods()) {
	    objectMethods.add(method);
	}
    }

    private static String[] functions = new String[]{"round", "now", "caseWhen", "lowerCase", "upperCase", "ifNull", "hourOf", "dayOf", "monthOf", "yearOf", "minuteOf", "secondOf", "countDays"};

    private static String[] aggregateFunctions = new String[]{"maxOf", "minOf", "sumOf", "countOf", "avgOf", "sumOfDistinct", "countOfDistinct", "avgOfDistinct", "countAll"};

    private static String[] anyOfs = new String[]{"anyOfProps", "anyOfModels", "anyOfValues", "anyOfParams", "anyOfExpressions"};

    private static String[] exists = new String[]{"exists", "notExists", "existsAnyOf", "notExistsAnyOf", "existsAllOf", "notExistsAllOf"};

    private Set<String> getMethods(final Class type) {
	final Set<String> result = new HashSet<String>();
	for (final Method method : type.getMethods()) {
	    if (!objectMethods.contains(method) && !"toString".equals(method.getName()) && !"getTokens".equals(method.getName())) {
		result.add(method.getName());
	    }
	}
	return result;
    }

    private String compare(final Set<String> set, final String[]...methods) {
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

    public void test_IPlainJoin(){
	final EntityQueryProgressiveInterfaces.IPlainJoin qry = query.select(AbstractEntity.class);
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "modelAsAggregate", "modelAsEntity" ,"where", "groupBy", "orderBy", "yield", "as", "join", "leftJoin"});
	assertNull(result, result);
    }

    public void test_IWhere0(){
	final EntityQueryProgressiveInterfaces.IWhere0 qry = query.select(AbstractEntity.class).where();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "begin", "notBegin", "beginExpr", "expr"}, exists, functions, anyOfs);
	assertNull(result, result);
    }

    public void test_IFunctionLastArgument_1_ICompleted_1_(){
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> qry = query.select(AbstractEntity.class).groupBy();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr"}, functions);
	assertNull(result, result);
    }

    public void test_IFunctionLastArgument_1_IOrder_2_ICompletedAndOrdered_2__1_(){
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> qry = query.select(AbstractEntity.class).orderBy();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr"}, functions);
	assertNull(result, result);
    }

    public void test_IFunctionYieldedLastArgument_1_IFirstYieldedItemAlias_2_ISubsequentCompletedAndYielded_2__1_(){
	final EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = query.select(AbstractEntity.class).yield();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result, result);
    }

    public void test_IFirstYieldedItemAlias_1_ISubsequentCompletedAndYielded_1_() {
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> qry = query.select(AbstractEntity.class).yield().prop("prop");
	final String result = compare(getMethods(qry.getClass()), new String[]{"as", "modelAsEntity", "modelAsPrimitive", "modelAsPrimitive"});
	assertNull(result, result);
    }

    public void test_(){
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = query.select(AbstractEntity.class).yield().secondOf();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr"}, functions);
	assertNull(result, result);
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> qry2 = query.select(AbstractEntity.class).where().secondOf();
	final String result2 = compare(getMethods(qry2.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr"}, functions);
	assertNull(result2, result2);

    }

    public void test_2(){
	final EntityQueryProgressiveInterfaces.IYieldExprItem0<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = query.select(AbstractEntity.class).yield().beginExpr();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result, result);
	final EntityQueryProgressiveInterfaces.IYieldExprItem1<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry2 = query.select(AbstractEntity.class).yield().beginExpr().beginExpr();
	final String result2 = compare(getMethods(qry2.getClass()), new String[]{"model", "prop", "param" ,"val", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result2, result2);

	final EntityQueryProgressiveInterfaces.IWhere0 where = query.select(AbstractEntity.class).where();
	final EntityQueryProgressiveInterfaces.IWhere1 a = where.begin();
	final EntityQueryProgressiveInterfaces.IWhere2 b = where.begin().begin();
	final EntityQueryProgressiveInterfaces.IWhere3 c = where.begin().begin().begin();
	final EntityQueryProgressiveInterfaces.ICompoundCondition2 d = where.begin().begin().begin().exists(null).end();
	final EntityQueryProgressiveInterfaces.ICompoundCondition1 e = where.begin().begin().begin().exists(null).end().end();
	final EntityQueryProgressiveInterfaces.ICompoundCondition0 e1 = where.begin().begin().begin().exists(null).end().end().end();

	final EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded qry11 = query.select(AbstractEntity.class).yield().prop("a").as("a").yield().prop("b").as("b");
	final EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded qry22 = query.select(AbstractEntity.class).yield().prop("a").as("a").yield().prop("b").as("b").yield().join("aa").as("bb");
    }

    public void test_yielding_aliases() {
	//
	final EntityResultQueryModel<AbstractEntity> model1 = query.select(AbstractEntity.class).yield().prop("prop1").modelAsEntity(AbstractEntity.class/*Should be prop1 entity type*/);
	//
	final PrimitiveResultQueryModel model2a = query.select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().modelAsPrimitive();
	final PrimitiveResultQueryModel model2b = query.select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().modelAsPrimitive(BigDecimal.class);
	//
	final EntityResultQueryModel<AbstractEntity> model3 = query.select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().as("sumOfProps").yield().model(null).as("newCalcProp").modelAsEntity(AbstractEntity.class);
	//
	final AggregatedResultQueryModel model4 = query.select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().as("sumOfProps").yield().model(null).as("newCalcProp").modelAsAggregate();
	//
	//aliases are required of joining is used
	query.select(AbstractEntity.class).as("a").join(AbstractEntity.class).as("b").on().prop("a.id").eq().prop("b.id").model();

    }

    public void rest() {

	query.select(AbstractEntity.class).yield().secondOf().beginExpr();
	query.select(AbstractEntity.class).yield().secondOf().val(100);
	query.select(AbstractEntity.class).yield().secondOf().param("param");
	query.select(AbstractEntity.class).yield().secondOf().prop("prop1");
	query.select(AbstractEntity.class).yield().secondOf().model(null);
	query.select(AbstractEntity.class).yield().secondOf().round();
	query.select(AbstractEntity.class).yield().secondOf().now();
	query.select(AbstractEntity.class).yield().secondOf().caseWhen();
	query.select(AbstractEntity.class).yield().secondOf().lowerCase();
	query.select(AbstractEntity.class).yield().secondOf().upperCase();
	query.select(AbstractEntity.class).yield().secondOf().ifNull();
	query.select(AbstractEntity.class).yield().secondOf().countDays();
	query.select(AbstractEntity.class).yield().secondOf().secondOf();
	query.select(AbstractEntity.class).yield().secondOf().minuteOf();
	query.select(AbstractEntity.class).yield().secondOf().hourOf();
	query.select(AbstractEntity.class).yield().secondOf().dayOf();
	query.select(AbstractEntity.class).yield().secondOf().monthOf();
	query.select(AbstractEntity.class).yield().secondOf().yearOf();


	query.select(AbstractEntity.class).yield().prop("prop").as("as");

	final EntityQueryProgressiveInterfaces.IYieldExprItem0<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a5 = query.select(AbstractEntity.class).yield().beginExpr();
	final EntityQueryProgressiveInterfaces.IRoundFunctionArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a6 = query.select(AbstractEntity.class).yield().round();
	final EntityQueryProgressiveInterfaces.IFunctionWhere0<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a8 = query.select(AbstractEntity.class).yield().caseWhen();
	final EntityQueryProgressiveInterfaces.IIfNullFunctionArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a11 = query.select(AbstractEntity.class).yield().ifNull();
	final EntityQueryProgressiveInterfaces.IDateDiffFunction<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a12 = query.select(AbstractEntity.class).yield().countDays();

	final EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered> b1 = query.select(AbstractEntity.class).orderBy().model(null);
	final EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered> b2 = query.select(AbstractEntity.class).orderBy().prop("prop");
	final EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered> b3 = query.select(AbstractEntity.class).orderBy().param("param");
	final EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered> b4 = query.select(AbstractEntity.class).orderBy().val(100);
	final EntityQueryProgressiveInterfaces.IExprOperand0<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> b5 = query.select(AbstractEntity.class).orderBy().beginExpr();
	final EntityQueryProgressiveInterfaces.IRoundFunctionArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> b6 = query.select(AbstractEntity.class).orderBy().round();
	final EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered> b7 = query.select(AbstractEntity.class).orderBy().now();
	final EntityQueryProgressiveInterfaces.IFunctionWhere0<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> b8 = query.select(AbstractEntity.class).orderBy().caseWhen();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> b9 = query.select(AbstractEntity.class).orderBy().lowerCase();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c1 = query.select(AbstractEntity.class).orderBy().upperCase();
	final EntityQueryProgressiveInterfaces.IIfNullFunctionArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c2 = query.select(AbstractEntity.class).orderBy().ifNull();
	final EntityQueryProgressiveInterfaces.IDateDiffFunction<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c3 = query.select(AbstractEntity.class).orderBy().countDays();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c4 = query.select(AbstractEntity.class).orderBy().secondOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c5 = query.select(AbstractEntity.class).orderBy().minuteOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c6 = query.select(AbstractEntity.class).orderBy().hourOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c7 = query.select(AbstractEntity.class).orderBy().dayOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> c8 = query.select(AbstractEntity.class).orderBy().monthOf();
	final EntityQueryProgressiveInterfaces.IBeginExpression<EntityQueryProgressiveInterfaces.IExprOperand0<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>>> c9 = query.select(AbstractEntity.class).orderBy().yearOf();

	final EntityQueryProgressiveInterfaces.ICompleted d1 = query.select(AbstractEntity.class).groupBy().model(null);
	final EntityQueryProgressiveInterfaces.ICompleted d2 = query.select(AbstractEntity.class).groupBy().prop("prop");
	final EntityQueryProgressiveInterfaces.ICompleted d3 = query.select(AbstractEntity.class).groupBy().param("param");
	final EntityQueryProgressiveInterfaces.ICompleted d4 = query.select(AbstractEntity.class).groupBy().val(100);
	final EntityQueryProgressiveInterfaces.IExprOperand0<EntityQueryProgressiveInterfaces.ICompleted> d5 = query.select(AbstractEntity.class).groupBy().beginExpr();
	final EntityQueryProgressiveInterfaces.IRoundFunctionArgument<EntityQueryProgressiveInterfaces.ICompleted> d6 = query.select(AbstractEntity.class).groupBy().round();
	final EntityQueryProgressiveInterfaces.ICompleted d7 = query.select(AbstractEntity.class).groupBy().now();
	final EntityQueryProgressiveInterfaces.IFunctionWhere0<EntityQueryProgressiveInterfaces.ICompleted> d8 = query.select(AbstractEntity.class).groupBy().caseWhen();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d9 = query.select(AbstractEntity.class).groupBy().lowerCase();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d0 = query.select(AbstractEntity.class).groupBy().upperCase();
	final EntityQueryProgressiveInterfaces.IIfNullFunctionArgument<EntityQueryProgressiveInterfaces.ICompleted> d11 = query.select(AbstractEntity.class).groupBy().ifNull();
	final EntityQueryProgressiveInterfaces.IDateDiffFunction<EntityQueryProgressiveInterfaces.ICompleted> d12 = query.select(AbstractEntity.class).groupBy().countDays();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d13 = query.select(AbstractEntity.class).groupBy().secondOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d14 = query.select(AbstractEntity.class).groupBy().minuteOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d15 = query.select(AbstractEntity.class).groupBy().hourOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d16 = query.select(AbstractEntity.class).groupBy().dayOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d17 = query.select(AbstractEntity.class).groupBy().monthOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> d18 = query.select(AbstractEntity.class).groupBy().yearOf();

	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q1 = query.select(AbstractEntity.class).where().model(null);
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q2 = query.select(AbstractEntity.class).where().prop("prop");
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q3 = query.select(AbstractEntity.class).where().param("param");
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q4 = query.select(AbstractEntity.class).where().val(100);
	final EntityQueryProgressiveInterfaces.IWhere1 q5 = query.select(AbstractEntity.class).where().begin();
	final EntityQueryProgressiveInterfaces.IWhere1 q6 = query.select(AbstractEntity.class).where().notBegin();
	final EntityQueryProgressiveInterfaces.ICompoundCondition0 q7 = query.select(AbstractEntity.class).where().exists(null);
	final EntityQueryProgressiveInterfaces.ICompoundCondition0 q8 = query.select(AbstractEntity.class).where().notExists(null);
	final EntityQueryProgressiveInterfaces.IExprOperand0<EntityQueryProgressiveInterfaces.IComparisonOperator0> q9 = query.select(AbstractEntity.class).where().beginExpr();
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q10 = query.select(AbstractEntity.class).where().anyOfModels();
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q11 = query.select(AbstractEntity.class).where().anyOfParams();
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q12 = query.select(AbstractEntity.class).where().anyOfProps();
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q13 = query.select(AbstractEntity.class).where().anyOfValues();
	final EntityQueryProgressiveInterfaces.IRoundFunctionArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q14 = query.select(AbstractEntity.class).where().round();
	final EntityQueryProgressiveInterfaces.IComparisonOperator0 q15 = query.select(AbstractEntity.class).where().now();
	final EntityQueryProgressiveInterfaces.IFunctionWhere0<EntityQueryProgressiveInterfaces.IComparisonOperator0> q16 = query.select(AbstractEntity.class).where().caseWhen();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q17 = query.select(AbstractEntity.class).where().lowerCase();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q18 = query.select(AbstractEntity.class).where().upperCase();
	final EntityQueryProgressiveInterfaces.IIfNullFunctionArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q19 = query.select(AbstractEntity.class).where().ifNull();
	final EntityQueryProgressiveInterfaces.IDateDiffFunction<EntityQueryProgressiveInterfaces.IComparisonOperator0> q20 = query.select(AbstractEntity.class).where().countDays();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q21 = query.select(AbstractEntity.class).where().secondOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q22 = query.select(AbstractEntity.class).where().minuteOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q23 = query.select(AbstractEntity.class).where().hourOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q24 = query.select(AbstractEntity.class).where().dayOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q25 = query.select(AbstractEntity.class).where().monthOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> q26 = query.select(AbstractEntity.class).where().yearOf();

	final EntityQueryProgressiveInterfaces.IJoin qry1  = query.select(AbstractEntity.class).as("alias");
	//query.select(AggregatedResultQueryModel... sourceQueryModels);
	//query.select(EntityResultQueryModel... sourceQueryModels);
	//query.select(EntityResultQueryModel sourceQueryModel, String alias)
    }

    public void done() {
	final EntityQueryProgressiveInterfaces.IPlainJoin qry = query.select(AbstractEntity.class);
	final EntityQueryProgressiveInterfaces.IWhere0 qry1 = query.select(AbstractEntity.class).where();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> qry2 = query.select(AbstractEntity.class).groupBy();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> qry3 = query.select(AbstractEntity.class).orderBy();
	final EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry4 = query.select(AbstractEntity.class).yield();

	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a1 = query.select(AbstractEntity.class).yield().model(null);
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a2 = query.select(AbstractEntity.class).yield().prop("prop");
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a3 = query.select(AbstractEntity.class).yield().param("param");
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a4 = query.select(AbstractEntity.class).yield().val(100);
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a7 = query.select(AbstractEntity.class).yield().now();
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> a19 = query.select(AbstractEntity.class).yield().join("joinAlias");

	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry5 = query.select(AbstractEntity.class).yield().secondOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a13 = query.select(AbstractEntity.class).yield().secondOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a14 = query.select(AbstractEntity.class).yield().minuteOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a15 = query.select(AbstractEntity.class).yield().hourOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a16 = query.select(AbstractEntity.class).yield().dayOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a17 = query.select(AbstractEntity.class).yield().monthOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a18 = query.select(AbstractEntity.class).yield().yearOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a = query.select(AbstractEntity.class).yield().maxOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> b = query.select(AbstractEntity.class).yield().minOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> c = query.select(AbstractEntity.class).yield().sumOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> d = query.select(AbstractEntity.class).yield().countOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> e = query.select(AbstractEntity.class).yield().avgOf();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a9 = query.select(AbstractEntity.class).yield().lowerCase();
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> a10 = query.select(AbstractEntity.class).yield().upperCase();


    }

//  public void test1(){
//	final UnorderedQueryModel m1 = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).model();
//
//	final EntityResultQueryModel<AbstractEntity> m2 = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).yield().prop("entityProperty1").modelAsEntity(AbstractEntity.class);
//
//	final PrimitiveResultQueryModel m3 = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).yield().sumOf().prop("decimalProperty1").modelAsPrimitive(BigDecimal.class);
//
//	final EntityResultQueryModel<AbstractEntity> m4 = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).groupBy().prop("entityProperty1").yield().sumOf().prop("decimalProperty1").as("newProp1").modelAsEntity(AbstractEntity.class);
//
//	final AggregatedResultQueryModel m5 = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).groupBy().prop("entityProperty1").yield().sumOf().prop("decimalProperty1").as("newProp1").yield().prop("entityProperty1").as("newProp2").modelAsAggregate();
//
//	final ua.com.fielden.platform.entity.query.model.QueryModel m1o = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).orderBy().prop("p1").desc().modelAsEntity(AbstractEntity.class);
//
//	//final ua.com.fielden.platform.entity.query.model.QueryModel m2o = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).yield().prop("entityProperty1").modelAsEntity(AbstractEntity.class);
//
//	//final ua.com.fielden.platform.entity.query.model.QueryModel m3o = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).yield().sumOf().prop("decimalProperty1").modelAsPrimitive(BigDecimal.class);
//
//	final ua.com.fielden.platform.entity.query.model.QueryModel m4o = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).groupBy().prop("entityProperty1").yield().sumOf().prop("decimalProperty1").as("newProp1").orderBy().prop("newProp1").desc().modelAsEntity(AbstractEntity.class);
//
//	final ua.com.fielden.platform.entity.query.model.QueryModel m5o = query.select(AbstractEntity.class).where().prop("p1").eq().val(1).groupBy().prop("entityProperty1").yield().sumOf().prop("decimalProperty1").as("newProp1").yield().prop("entityProperty1").as("newProp2").orderBy().prop("newProp1").desc().orderBy().prop("newProp2").asc().modelAsEntity(AbstractEntity.class);
//
//}
}

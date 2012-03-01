package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import static ua.com.fielden.platform.entity.query.fluent.query.select;


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

    private static String[] allOfs = new String[]{"allOfProps", "allOfModels", "allOfValues", "allOfParams", "allOfExpressions"};

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
	final EntityQueryProgressiveInterfaces.IPlainJoin qry = select(AbstractEntity.class);
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "modelAsAggregate", "modelAsEntity" ,"where", "groupBy", "yield", "as", "join", "leftJoin"});
	assertNull(result, result);
    }

    public void test_IWhere0(){
	final EntityQueryProgressiveInterfaces.IWhere0 qry = select(AbstractEntity.class).where();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "begin", "notBegin", "beginExpr", "expr"}, exists, functions, anyOfs, allOfs);
	assertNull(result, result);
    }

    public void test_IFunctionLastArgument_1_ICompleted_1_(){
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.ICompleted> qry = select(AbstractEntity.class).groupBy();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param" , "iParam", "val", "iVal", "beginExpr", "expr"}, functions);
	assertNull(result, result);
    }

//    public void test_IFunctionLastArgument_1_IOrder_2_ICompletedAndOrdered_2__1_(){
//	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IOrder<EntityQueryProgressiveInterfaces.ICompletedAndOrdered>> qry = select(AbstractEntity.class).orderBy();
//	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr"}, functions);
//	assertNull(result, result);
//    }

    public void test_IFunctionYieldedLastArgument_1_IFirstYieldedItemAlias_2_ISubsequentCompletedAndYielded_2__1_(){
	final EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = select(AbstractEntity.class).yield();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result, result);
    }

    public void test_IFirstYieldedItemAlias_1_ISubsequentCompletedAndYielded_1_() {
	final EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded> qry = select(AbstractEntity.class).yield().prop("prop");
	final String result = compare(getMethods(qry.getClass()), new String[]{"as", "modelAsEntity", "modelAsPrimitive", "modelAsPrimitive"});
	assertNull(result, result);
    }

    public void test_(){
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = select(AbstractEntity.class).yield().secondOf();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr"}, functions);
	assertNull(result, result);
	final EntityQueryProgressiveInterfaces.IFunctionLastArgument<EntityQueryProgressiveInterfaces.IComparisonOperator0> qry2 = select(AbstractEntity.class).where().secondOf();
	final String result2 = compare(getMethods(qry2.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr"}, functions);
	assertNull(result2, result2);

    }

    public void test_2(){
	final EntityQueryProgressiveInterfaces.IYieldExprItem0<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry = select(AbstractEntity.class).yield().beginExpr();
	final String result = compare(getMethods(qry.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result, result);
	final EntityQueryProgressiveInterfaces.IYieldExprItem1<EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias<EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded>> qry2 = select(AbstractEntity.class).yield().beginExpr().beginExpr();
	final String result2 = compare(getMethods(qry2.getClass()), new String[]{"model", "prop", "param", "iParam", "val", "iVal", "beginExpr", "expr", "join"}, functions, aggregateFunctions);
	assertNull(result2, result2);

	final EntityQueryProgressiveInterfaces.IWhere0 where = select(AbstractEntity.class).where();
	final EntityQueryProgressiveInterfaces.IWhere1 a = where.begin();
	final EntityQueryProgressiveInterfaces.IWhere2 b = where.begin().begin();
	final EntityQueryProgressiveInterfaces.IWhere3 c = where.begin().begin().begin();
	final EntityQueryProgressiveInterfaces.ICompoundCondition2 d = where.begin().begin().begin().exists(null).end();
	final EntityQueryProgressiveInterfaces.ICompoundCondition1 e = where.begin().begin().begin().exists(null).end().end();
	final EntityQueryProgressiveInterfaces.ICompoundCondition0 e1 = where.begin().begin().begin().exists(null).end().end().end();

	final EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded qry11 = select(AbstractEntity.class).yield().prop("a").as("a").yield().prop("b").as("b");
	final EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded qry22 = select(AbstractEntity.class).yield().prop("a").as("a").yield().prop("b").as("b").yield().join("aa").as("bb");
    }

    public void test_yielding_aliases() {
	//
	final EntityResultQueryModel<AbstractEntity> model1 = select(AbstractEntity.class).yield().prop("prop1").modelAsEntity(AbstractEntity.class/*Should be prop1 entity type*/);
	//
	final PrimitiveResultQueryModel model2a = select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().modelAsPrimitive();
	//
	final EntityResultQueryModel<AbstractEntity> model3 = select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().as("sumOfProps").yield().model(null).as("newCalcProp").modelAsEntity(AbstractEntity.class);
	//
	final AggregatedResultQueryModel model4 = select(AbstractEntity.class).yield().beginExpr().prop("prop1").add().prop("prop2").endExpr().as("sumOfProps").yield().model(null).as("newCalcProp").modelAsAggregate();
	//
	//aliases are required of joining is used
	select(AbstractEntity.class).as("a").join(AbstractEntity.class).as("b").on().prop("a.id").eq().prop("b.id").model();

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

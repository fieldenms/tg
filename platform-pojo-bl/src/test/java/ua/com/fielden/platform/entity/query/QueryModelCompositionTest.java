package ua.com.fielden.platform.entity.query;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelCompositionTest extends BaseEntQueryTCase {

    @Test
    @Ignore
    public void test_query_model1() {
	final AggregatedResultQueryModel a =
		query.select(TgWorkOrder.class).as("wo")
		.where().beginExpr().beginExpr().beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr().add().prop("wo.yearlyCost.amount").endExpr().div().val(12).endExpr().gt().val(1000)
		.and()
		.begin()
		.beginExpr().param("param1").mult().beginExpr().param("v").add().prop("wo.vehicle.initPrice").endExpr().endExpr().eq().prop("wo.insuranceAmount")
		.and()
		.val("a").isNull().and()
		.prop("wo.insuranceAmount").isNotNull()
		.end()
		.modelAsAggregate();
	qb.generateEntQuery(a);//.getPropNames();

	query.select(TgWorkOrder.class).where()
		.upperCase()
				.beginExpr()
					.prop("bbb").add().prop("aaa")
				.endExpr()
			.like()
			.upperCase().val("AaA")
		.and()
			.prop("SomeDate").gt().now()
		.and()
			.yearOf().prop("LastChange").eq().round()
								.beginExpr()
									.prop("prop1").add().prop("prop2")
								.endExpr().to(1)
		.and()
			.param("a").eq().ifNull().prop("interProp").then().val(1)
		.and()
			.prop("aaa").eq().countDays().between().beginExpr().beginExpr().prop("Start1").add().prop("Start2").endExpr().endExpr().and().ifNull().prop("End").then().now()
		.and()
			.prop("haha").in().props("p1", "p2", "p3")
		.and()
			.param("AAA").in().values(1, 2, 3)
		.modelAsAggregate();

	final String expString = "SELECT\nFROM ua.com.fielden.platform.entity.AbstractEntity AS a\nWHERE (((($a + :v)))) > 1 AND ((:a * (:v + $c)) = $d AND $ad IS NOT NULL) AND UPPERCASE($bbb + $aaa) LIKE UPPERCASE(AaA) AND $SomeDate > NOW() AND YEAR($LastChange) = ROUND(($prop1 + $prop2), 1) AND :a = COALESCE($interProp, 1) AND $aaa = DATEDIFF(COALESCE($End, NOW()), (($Start1 + $Start2))) AND $haha IN ($p1, $p2, $p3) AND :AAA IN (1, 2, 3)";

	final Object b = query.select(a).as("a_alias").where().prop("a_alias.p1").eq().val(100).and().prop("a_alias.p2").like().anyOfValues("%AC", "%AB", "DD%").model();
    }

    @Ignore
    @Test
    public void test_expressions() {
	assertEquals("Incorrect expression model string representation", "1 + :a + 2", query.expr().val(1).add().param("a").add().val(2).model().toString());
	assertEquals("Incorrect expression model string representation", "1", query.expr().val(1).model().toString());
	assertEquals("Incorrect expression model string representation", "1 + :a + AVG(SUM($fuelCost)) + NOW() * SECOND(NOW())", query.expr().val(1).add().param("a").add().avgOf().expr(query.expr().sumOf().prop("fuelCost").model()).add().now().mult().expr(query.expr().secondOf().now().model()).model().toString());
    }

    @Ignore
    @Test
    public void test_query_model3() {
	final Object a = query.select(AbstractEntity.class).as("a")
	    .join(AbstractEntity.class).as("b")
	    .on().prop("a").eq().prop("v").and()
	    .begin().begin().beginExpr().prop("a").add().param("v").endExpr().eq().prop("c").end().end()
	    .and()
	    .expr(null).eq().expr(null).and()
	    .prop("a")
	    	.eq()
	    .beginExpr()
	    	.prop("1").sub().prop("2")
	    .endExpr()
	    	.and().anyOfModels(null, null).eq().beginExpr().prop("a").endExpr()
	    	.and()
	    .beginExpr().beginExpr().beginExpr().beginExpr().param("1").add().param("2").endExpr().add().beginExpr().param("1").div().prop("2").endExpr().endExpr().endExpr().endExpr().eq().val(false)
	    .and()
	    .notExists(null)
	    .where()
	    .countDays().between().prop("1").and().prop("2").eq().beginExpr().countDays().between().model(null).and().model(null).endExpr().and()
	    .prop("1").ne().param("a")
	.and().beginExpr().prop("1").add().beginExpr().prop("1").add().prop("2").add().beginExpr().beginExpr().param("1").div().param("2").endExpr().div().beginExpr().param("3").endExpr().endExpr().endExpr().sub().prop("2").endExpr().eq().prop("a")
	.and().beginExpr().prop("2").endExpr().eq().beginExpr().prop("1").sub().prop("2").endExpr()
	.and().begin().prop("asa").isNull().end()
	.and().beginExpr().prop("dsd").endExpr().eq().param("ss").and().prop("a").like().beginExpr().val(1).add().val(3).endExpr().and()
	.begin().beginExpr().prop("1").sub().prop("2").endExpr().eq().prop("3").and()
		.begin().beginExpr().prop("1").sub().val(2).endExpr().isNotNull().and()
			.begin().exists(null).and().beginExpr().prop("1").div().val(2).mult().val(3).endExpr().eq().beginExpr().prop("1").add().prop("2").endExpr()
			.end()
		.end()
	.end().and()
	.countDays().between().beginExpr().beginExpr().beginExpr().val(1).sub().val(2).endExpr().endExpr().endExpr()
	.and()
	.beginExpr().beginExpr().beginExpr().param("1").div().param("2").endExpr().endExpr().endExpr()
	.eq()
	.countDays().between().prop("1").and().now()
	.and()
	.beginExpr().param("1").sub().upperCase().prop("1").endExpr().gt().all(null).and().val(1).isNotNull()
	.and()
	.beginExpr().prop("1").add().prop("2").endExpr().in().model(null)
	.and()
	.ifNull().prop("1").then().upperCase().val("AaAv")
		.eq()
	.beginExpr().ifNull().beginExpr().prop("1").add().prop("2").endExpr().then().now().sub().val(1).endExpr()
	.and()
	.anyOfModels(null, null).eq().prop("a")
	.and()
	.begin().begin().prop("a").eq().beginExpr().beginExpr().beginExpr().beginExpr().now().endExpr().endExpr().endExpr().endExpr().and().begin().beginExpr().beginExpr().beginExpr().beginExpr().now().endExpr().endExpr().endExpr().endExpr().eq().beginExpr().beginExpr().beginExpr().beginExpr().param("a").endExpr().endExpr().endExpr().add().beginExpr().beginExpr().beginExpr().prop("a").endExpr().endExpr().endExpr().endExpr().end().end().end()
	.or()
	.caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().ge().param("bb")
	.and()
//	.begin().begin().condition(null).and().condition(null).or().prop("1").isNotNull().and().condition(null).and().prop("2").ge().caseWhen().condition(null).then().val(1).end().end().end()
//	.and()
//	.condition(null)
//	.and()
	.beginExpr().prop("a").add().prop("b").endExpr().in().props()
	.groupBy().param("aaa")
	.groupBy().prop("a")
	.groupBy().beginExpr().param("11").add().prop("2").mult().val(100).endExpr()
	.yield().param("a").as("a").yield().param("b").as("b")
	.yield().countDays().between().model(null).and().beginExpr().beginExpr().beginExpr().beginExpr().param("1").endExpr().endExpr().endExpr().endExpr().as("myExp")
	.yield().beginExpr().beginExpr().ifNull().prop("1").then().now().endExpr().endExpr().as("mySecondExp")
	.yield().beginExpr().avgOf().beginExpr().prop("1").add().param("2").endExpr().add().val(3).endExpr().as("avg_from1+3")
	.yield().avgOf().beginExpr().beginExpr().prop("1").add().prop("2").endExpr().endExpr().as("avg_of_1+2")
	.yield().beginExpr().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().countDays().between().prop("1").and().val(100).endExpr().endExpr().endExpr().endExpr().add().val(2).endExpr().div().val(100).endExpr().as("alias")
	.yield().beginExpr().beginExpr().sumOf().prop("1").add().avgOf().prop("2").add().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().val(2).endExpr().endExpr().endExpr().endExpr().endExpr().endExpr().add().avgOf().beginExpr().val(2).add().prop("p1").endExpr().endExpr().as("alias")
	.yield().caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().as("aaa")
	.yield().round().prop("a").to(10).as("1")
	.yield().param("aa").as("aaa")
	.yield().countOf().prop("1").as("a")
	.yield().yearOf().now().as("b")
	.yield().join("b").as("c")
	.yield().countAll().as("recCount")
	.orderBy().prop("1").asc()
	.orderBy().model(null).asc()
	//.model()
	;
    }



//    @Test
//    public void test_simple_query_model_22a() {
//	final EntityResultQueryModel<TgVehicle> subQry0 = query.select(TgVehicle.class).where().val(1).isNotNull().model();
//	final EntityResultQueryModel<TgVehicle> subQry1 = query.select(TgVehicle.class).where().exists(subQry0).model();
//	final EntityResultQueryModel<TgWorkOrder> subQry2 = query.select(TgWorkOrder.class).model();
//	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().existsAnyOf(subQry1, subQry2).model();
//	final List<EntQuery> exp = Arrays.asList(new EntQuery[]{qb.generateEntQuery(subQry0), qb.generateEntQuery(subQry2)});
//	assertEquals("models are different", exp, qb.generateEntQuery(qry).getLeafSubqueries());
//    }
}
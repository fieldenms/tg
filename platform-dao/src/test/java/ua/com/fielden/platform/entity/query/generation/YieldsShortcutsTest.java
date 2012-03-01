package ua.com.fielden.platform.entity.query.generation;

import org.junit.Test;

import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class YieldsShortcutsTest extends BaseEntQueryTCase {

    @Test
    public void test_yield_of_property_as_result_entity() {
	assertModelsEquals(//
		select(VEHICLE).yield().prop("model.make").modelAsEntity(MAKE), //

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		yield().prop("model.make").as("id").modelAsEntity(MAKE));
    }

    @Test
    public void test_model_with_no_explicit_yields() {
	assertModelsEquals(//
		select(VEHICLE).model(),

		select(VEHICLE). //
		yield().prop("id").as("id"). //
		yield().prop("version").as("version"). //
		yield().prop("key").as("key"). //
		yield().prop("desc").as("desc"). //
		yield().prop("replacedBy").as("replacedBy"). //
		yield().prop("initDate").as("initDate"). //
		yield().prop("model").as("model"). //
		yield().prop("station").as("station"). //
		yield().prop("price.amount").as("price.amount"). //
		yield().prop("purchasePrice.amount").as("purchasePrice.amount"). //
		modelAsEntity(VEHICLE));
    }

    @Test
    public void test_model_with_no_explicit_yields_and_explicit_joins() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		model(), //

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		yield().prop("id").as("id"). //
		yield().prop("version").as("version"). //
		yield().prop("key").as("key"). //
		yield().prop("desc").as("desc"). //
		yield().prop("replacedBy").as("replacedBy"). //
		yield().prop("initDate").as("initDate"). //
		yield().prop("model").as("model"). //
		yield().prop("station").as("station"). //
		yield().prop("price.amount").as("price.amount"). //
		yield().prop("purchasePrice.amount").as("purchasePrice.amount"). //
		modelAsEntity(VEHICLE));
    }

    @Test
    public void test_model_with_subquery() {
	assertModelsEquals( //
		select(VEHICLE). //
		where().exists(
			select(MAKE). //
			where().prop("key").like().val("MERC%").and().prop("model.make").eq().prop("id"). //
			model()). //
		yield().prop("key").modelAsPrimitive(), //

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().exists(
			select(MAKE). //
			where().prop("key").like().val("MERC%").and().prop("model.make").eq().prop("id"). //
			yield().prop("id").as("id").modelAsEntity(MAKE)). //
		yield().prop("key").modelAsPrimitive());
    }

    @Test
    public void test_model_with_expression() {
	select(VEHICLE).
	where().beginExpr().val(100).mult().model(
		select(FUEL_USAGE).yield().sumOf().prop("qty").modelAsPrimitive()
		).endExpr().ge().val(1000).model();

//	assertModelsEquals( //
//		select(VEHICLE). //
//		where().beginExpr().model(
//			select(VEHICLE). //
//			yield().prop("price")
//			where().prop("key").like().val("MERC%").and().prop("model.make").eq().prop("id"). //
//			model()). //
//		yield().prop("key").modelAsPrimitive(), //
//
//		select(VEHICLE). //
//		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
//		where().exists(
//			select(MAKE). //
//			where().prop("key").like().val("MERC%").and().prop("model.make").eq().prop("id"). //
//			yield().prop("id").as("id").modelAsEntity(MAKE)). //
//		yield().prop("key").modelAsPrimitive());
    }


}
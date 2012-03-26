package ua.com.fielden.platform.entity.query;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class QueryShortcutsTest extends BaseEntQueryTCase {

    //////////////////////////////////////////////////// ANY/ALL shortcuts ///////////////////////////////////////////
    @Test
    public void test_multiple_vs_single_comparison_test() {
	assertModelsEquals(//
	select(VEHICLE).where().anyOfProps("key", "desc").eq().val("MERC").model(), //

	select(VEHICLE).where().begin().prop("key").eq().val("MERC").or().prop("desc").eq().val("MERC").end().model());
    }

    @Test
    public void test_multiple_vs_single_comparison_test_with_only_one_operand() {
	assertModelsEquals(//
	select(VEHICLE).where().anyOfProps("model").eq().val("MERC").model(), //

	select(VEHICLE).where().begin().prop("model").eq().val("MERC").end().model());
    }

    @Test
    public void test_single_vs_multiple_comparison_test() {
	assertModelsEquals(//
	select(VEHICLE).where().prop("model").eq().anyOfValues("MERC", "AUDI").model(), //

	select(VEHICLE).where().begin().prop("model").eq().val("MERC").or().prop("model").eq().val("AUDI").end().model());
    }

    @Test
    public void test_single_vs_multiple_comparison_test_with_only_one_operand() {
	assertModelsEquals(//
	select(VEHICLE).where().prop("model").eq().anyOfValues("MERC").model(), //

	select(VEHICLE).where().begin().prop("model").eq().val("MERC").end().model());
    }

    @Test
    public void test_all_of_vs_any_of_like_test() {
	assertModelsEquals(//
	select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues("MERC%", "AUDI%", "BMW%").model(), //

	select(VEHICLE).where().begin(). //
	begin().prop("model.key").like().val("MERC%").or().prop("model.key").like().val("AUDI%").or().prop("model.key").like().val("BMW%").end().and(). //
	begin().prop("model.make.key").like().val("MERC%").or().prop("model.make.key").like().val("AUDI%").or().prop("model.make.key").like().val("BMW%").end(). //
	end().model());
    }

    @Ignore
    @Test
    public void test_ignore_in_multiple_vs_single_like_test() {
	// TODO implement anyOfIValues
	final String[] values = new String[] { null, null, null };
	assertModelsEquals(//
	select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues(values).model(), //

	select(VEHICLE).where().begin(). //
	begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end().and(). //
	begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end(). //
	end().model());
    }

    @Test
    public void test_set_test_with_values_and_multiple_operand() {
	assertModelsEquals(//
	select(VEHICLE).where().anyOfProps("key", "desc").in().values("sta1", "sta2").model(), //

	select(VEHICLE).where().begin().prop("key").in().values("sta1", "sta2").or().prop("desc").in().values("sta1", "sta2").end().model());
    }

    @Test
    public void test_multiple_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	assertModelsEquals(//
	select(VEHICLE).where().anyOfProps("key", "desc").eq().any(vehModels).model(), //

	select(VEHICLE).where().begin().prop("key").eq().any(vehModels).or().prop("desc").eq().any(vehModels).end().model());
    }

    //////////////////////////////////////////////// YIELDS shortcuts /////////////////////////////////////////////////////

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
		yield().prop("active").as("active"). //
		yield().prop("leased").as("leased"). //
		yield().prop("lastMeterReading").as("lastMeterReading"). //
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
		yield().prop("active").as("active"). //
		yield().prop("leased").as("leased"). //
		yield().prop("lastMeterReading").as("lastMeterReading"). //
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
    @Ignore
    public void test_model_with_expression() {
	select(VEHICLE).
	where().beginExpr().val(100).mult().model(
		select(FUEL_USAGE).yield().sumOf().prop("qty").modelAsPrimitive()
		).endExpr().ge().val(1000).model();
    }


    ///////////////////////////////////////////////////////////// DOT.NOTATION /////////////////////////////////////////////////
    @Test
    public void test_that_no_additional_joins_are_made_in_case_of_id_property() {
	assertModelsDifferent(//
		select(VEHICLE). //
		where().prop("model.id").eq().val(100).model(),

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.id").eq().val(100).model());
    }

    @Test
    public void test_1_1_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		where().prop("model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_1_1_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		where().prop("replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_1_2_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(MODEL).as("replacedBy.model").on().prop("replacedBy.model").eq().prop("replacedBy.model.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_1_2_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(VEHICLE).as("replacedBy.replacedBy").on().prop("replacedBy.replacedBy").eq().prop("replacedBy.replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model());
    }


    @Test
    public void test_1_3_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(VEHICLE).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		join(VEHICLE).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		join(MODEL).as("rb.model").on().prop("rb.model").eq().prop("rb.model.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_1_3_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(VEHICLE).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		join(VEHICLE).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		leftJoin(VEHICLE).as("rb.replacedBy").on().prop("rb.replacedBy").eq().prop("rb.replacedBy.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_2_1_1() {
	assertModelsEquals(//
		select(select(VEHICLE).model()). //
		where().prop("model.key").eq().val("SPR 318").model(),

		select(select(VEHICLE).model()). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_2_1_2() {
	assertModelsEquals(//
		select(select(VEHICLE).model()). //
		where().prop("replacedBy.key").eq().val("CAR1").model(),

		select(select(VEHICLE).model()). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_2_2_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(select(VEHICLE).model()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		leftJoin(select(VEHICLE).model()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(MODEL).as("replacedBy.model").on().prop("replacedBy.model").eq().prop("replacedBy.model.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_2_2_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(select(VEHICLE).model()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		leftJoin(select(VEHICLE).model()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(VEHICLE).as("replacedBy.replacedBy").on().prop("replacedBy.replacedBy").eq().prop("replacedBy.replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_2_3_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(select(VEHICLE).model()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		join(select(VEHICLE).model()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		join(MODEL).as("rb.model").on().prop("rb.model").eq().prop("rb.model.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_2_3_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(select(VEHICLE).model()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		join(select(VEHICLE).model()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		leftJoin(VEHICLE).as("rb.replacedBy").on().prop("rb.replacedBy").eq().prop("rb.replacedBy.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model());
    }

    EntityResultQueryModel<TgVehicle> getReversedVehicleModel() {
	return
		select(VEHICLE). //
		join(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		yield().prop("replacedBy.id").as("replacedBy"). //
		yield().prop("model.id").as("model"). //
		yield().prop("id").as("id"). //
		yield().prop("version").as("version"). //
		yield().prop("key").as("key"). //
		yield().prop("desc").as("desc"). //
		yield().prop("initDate").as("initDate"). //
		yield().prop("station").as("station"). //
		yield().prop("price.amount").as("price.amount"). //
		yield().prop("purchasePrice.amount").as("purchasePrice.amount"). //
		yield().prop("active").as("active"). //
		yield().prop("leased").as("leased"). //
		yield().prop("lastMeterReading").as("lastMeterReading"). //
		modelAsEntity(VEHICLE);
    }

    @Test
    public void test_3_1_1() {
	assertModelsEquals(//
		select(getReversedVehicleModel()). //
		where().prop("model.key").eq().val("SPR 318").model(),

		select(getReversedVehicleModel()). //
		leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_3_1_2() {
	assertModelsEquals(//
		select(getReversedVehicleModel()). //
		where().prop("replacedBy.key").eq().val("CAR1").model(),

		select(getReversedVehicleModel()). //
		join(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_3_2_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(getReversedVehicleModel()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		leftJoin(getReversedVehicleModel()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(MODEL).as("replacedBy.model").on().prop("replacedBy.model").eq().prop("replacedBy.model.id"). //
		where().prop("replacedBy.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_3_2_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(getReversedVehicleModel()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		leftJoin(getReversedVehicleModel()).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(VEHICLE).as("replacedBy.replacedBy").on().prop("replacedBy.replacedBy").eq().prop("replacedBy.replacedBy.id"). //
		where().prop("replacedBy.replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test_3_3_1() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(getReversedVehicleModel()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model(),

		select(VEHICLE). //
		join(getReversedVehicleModel()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		leftJoin(MODEL).as("rb.model").on().prop("rb.model").eq().prop("rb.model.id"). //
		where().prop("rb.model.key").eq().val("SPR 318").model());
    }

    @Test
    public void test_3_3_2() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(getReversedVehicleModel()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model(),

		select(VEHICLE). //
		join(getReversedVehicleModel()).as("rb").on().prop("replacedBy").eq().prop("rb.id"). //
		join(VEHICLE).as("rb.replacedBy").on().prop("rb.replacedBy").eq().prop("rb.replacedBy.id"). //
		where().prop("rb.replacedBy.key").eq().val("CAR1").model());
    }

    @Test
    public void test2() {
	assertModelsEquals(//
		select(VEHICLE). //
		where().prop("model.make.key").eq().val("MERC").model(),

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test3() {
	assertModelsEquals(//
		select(VEHICLE). //
		where().prop("station.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model(),

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		leftJoin(ORG5).as("station").on().prop("station").eq().prop("station.id"). //
		where().prop("station.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA").model();

	assertModelsEquals(//
		select(sourceQry). //
		where().prop("model.make.key").eq().val("MERC").model(),

		select(sourceQry). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test5() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA").model();

	assertModelsEquals(//
		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		where().prop("s.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model(), //

		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("s.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test6() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").model();

	assertModelsEquals(//
		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		where().prop("s.parent.key").like().val("AA%").model(), //

		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		leftJoin(ORG4).as("s.parent").on().prop("s.parent").eq().prop("s.parent.id"). //
		where().prop("s.parent.key").like().val("AA%").model());

    }

    @Test
    public void test7() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").model();

	assertModelsEquals(//
		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		where().prop("s.parent.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model(), //

		select(sourceQry). //
		leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		leftJoin(ORG4).as("s.parent").on().prop("s.parent").eq().prop("s.parent.id"). //
		where().prop("s.parent.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test8() {
	assertModelsEquals(//
		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.make.key").eq().val("MERC").model(), //

		select(VEHICLE). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test9() {
	// demonstrate how user can misuse explicit joining and get incorrect result (used leftJoin instead of innerJoin) in case of vice-verse the result will be even worse - incomplete result set
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		where().prop("model.make.key").eq().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test10() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		where().prop("model.make.key").eq().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test11() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		where().prop("model.make.key").eq().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }

    @Test
    public void test12a() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		where().prop("rv.model.make.key").ne().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		leftJoin(MODEL).as("rv.model").on().prop("rv.model").eq().prop("rv.model.id"). //
		leftJoin(MAKE).as("rv.model.make").on().prop("rv.model.make").eq().prop("rv.model.make.id"). //
		where().prop("rv.model.make.key").ne().val("MERC").model());
    }

    @Test
    public void test12b() {
	assertModelsEquals(//
		select(VEHICLE). //
		where().prop("replacedBy.model.make.key").ne().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
		leftJoin(MODEL).as("replacedBy.model").on().prop("replacedBy.model").eq().prop("replacedBy.model.id"). //
		leftJoin(MAKE).as("replacedBy.model.make").on().prop("replacedBy.model.make").eq().prop("replacedBy.model.make.id"). //
		where().prop("replacedBy.model.make.key").ne().val("MERC").model());
    }

    @Test
    public void test12() {
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv.model.make.key").ne().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		leftJoin(MODEL).as("rv.model").on().prop("rv.model").eq().prop("rv.model.id"). //
		leftJoin(MAKE).as("rv.model.make").on().prop("rv.model.make").eq().prop("rv.model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv.model.make.key").ne().val("MERC").model());
    }

    @Test
    public void test13() {
	// this illustrates the case of records being ate by explicit IJ after explicit LJ and how implicit joins are generated for (sub)properties of explicit IJ (aliased as rv2)
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
		join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		join(MODEL).as("rv2.model").on().prop("rv2.model").eq().prop("rv2.model.id"). //
		join(MAKE).as("rv2.model.make").on().prop("rv2.model.make").eq().prop("rv2.model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model());
    }

    @Test
    public void test14() {
	// this illustrates the case of records being ate by explicit IJ after explicit LJ and how implicit joins are generated for (sub)properties of explicit IJ (aliased as rv2)
	assertModelsEquals(//
		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv.id"). //
		join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2.id"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model(), //

		select(VEHICLE). //
		leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv.id"). //
		join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2.id"). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		join(MODEL).as("rv2.model").on().prop("rv2.model").eq().prop("rv2.model.id"). //
		join(MAKE).as("rv2.model.make").on().prop("rv2.model.make").eq().prop("rv2.model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model());
    }

    ////////////////////////////////////////////////////////////  User-based data filtering ////////////////////////////////////////////////////////////////////////

    @Test
    public void test15() {
	assertModelsEqualsAccordingUserDataFiltering(//
		select(VEHICLE). //
		where().prop("model.make.key").eq().val("MERC").model(),

		select(select(VEHICLE).	where().prop("key").notLike().val("A%").model()). //
		join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
		join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
		where().prop("model.make.key").eq().val("MERC").model());
    }
}
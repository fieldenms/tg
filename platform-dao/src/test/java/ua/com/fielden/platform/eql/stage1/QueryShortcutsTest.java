package ua.com.fielden.platform.eql.stage1;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.BaseEntQueryTCase1;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class QueryShortcutsTest extends BaseEntQueryTCase1 {

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
}
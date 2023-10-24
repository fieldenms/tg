package ua.com.fielden.platform.eql.stage1;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlStage1TestCase;
import ua.com.fielden.platform.eql.stage1.queries.ResultQuery1;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;

public class QueryShortcutsTest extends EqlStage1TestCase {


    public static <T extends AbstractEntity<?>> void assertModelsEquals(final EntityResultQueryModel<T> shortcutModel, final EntityResultQueryModel<T> explicitModel) {
        final ResultQuery1 shortcutQry = resultQry(shortcutModel);
        final ResultQuery1 explicitQry = resultQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }
    
    public static void assertModelsEquals(final AggregatedResultQueryModel shortcutModel, final AggregatedResultQueryModel explicitModel) {
        final ResultQuery1 shortcutQry = resultQry(shortcutModel);
        final ResultQuery1 explicitQry = resultQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }
    
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
        final EntityResultQueryModel<TeVehicleModel> vehModels = select(MODEL).model();
        assertModelsEquals(//
        select(VEHICLE).where().anyOfProps("key", "desc").eq().any(vehModels).model(), //

        select(VEHICLE).where().begin().prop("key").eq().any(vehModels).or().prop("desc").eq().any(vehModels).end().model());
    }
}
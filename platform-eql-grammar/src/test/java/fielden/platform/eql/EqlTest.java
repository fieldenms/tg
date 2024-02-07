package fielden.platform.eql;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;

import static fielden.platform.eql.Eql.select;
//import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
//import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EqlTest {

    @Test
    public void eql3_query_executes_correctly8() {
        select(TeWorkOrder.class).where().prop("makeKey").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly9() {
        select(TeWorkOrder.class).where().prop("make.key").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly10() {
        select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly11() {
        select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey2").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly12() {
        select(TeWorkOrder.class).where().anyOfProps("model.makeKey2").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly13() {
        select(TeWorkOrder.class).where().anyOfProps("makeKey2").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly14() {
        select(TeVehicle.class).where().anyOfProps("calcModel").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly15() {
        select(TeVehicleMake.class).where().anyOfProps("c7").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly16() {
        select(TeVehicleMake.class).where().anyOfProps("c6").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly17() {
        select(TeVehicleMake.class).where().anyOfProps("c3").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly18() {
        select(TeVehicleMake.class).where().anyOfProps("c8").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly19() {
        select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly20() {
        select(TeWorkOrder.class).where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly21() {
        select(TeVehicle.class).where().anyOfProps("modelKey", "modelDesc").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly22() {
        select(TeVehicle.class).where().anyOfProps("modelMakeKey2", "make.key").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly23() {
        select(TeVehicle.class).where().anyOfProps("replacedBy.modelMakeKey2", "modelMakeKey2").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly24() {
        select(TeVehicle.class).where().anyOfProps("modelMakeKey2").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly25() {
        select(TeVehicle.class).where().anyOfProps("modelMakeKey3").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly26() {
        select(TeVehicle.class).where().anyOfProps("modelMakeKey4").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly27() {
        select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key"
        ).isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly28() {
        select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key",
                "replacedBy.modelKey",
                "replacedBy.modelKey2",
                "replacedBy.modelDesc",
                "replacedBy.stationName",
                "replacedBy.modelMakeDesc",
                "replacedBy.modelMake",
                "replacedBy.modelMake2",
                "replacedBy.modelMakeKey",
                "replacedBy.modelMakeKey2",
                "replacedBy.modelMakeKey3",
                "replacedBy.modelMakeKey4",
                "replacedBy.modelMakeKey5",
                "replacedBy.modelMakeKey6",
                "replacedBy.modelMakeKey7",
                "replacedBy.modelMakeKey8",
                "replacedBy.make.key"
        ).isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly29() {
        select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key",
                "replacedBy.modelKey",
                "replacedBy.modelKey2",
                "replacedBy.modelDesc",
                "replacedBy.stationName",
                "replacedBy.modelMakeDesc",
                "replacedBy.modelMake",
                "replacedBy.modelMake2",
                "replacedBy.modelMakeKey",
                "replacedBy.modelMakeKey2",
                "replacedBy.modelMakeKey3",
                "replacedBy.modelMakeKey4",
                "replacedBy.modelMakeKey5",
                "replacedBy.modelMakeKey6",
                "replacedBy.modelMakeKey7",
                "replacedBy.modelMakeKey8",
                "replacedBy.make.key",
                "replacedBy.replacedBy.modelKey",
                "replacedBy.replacedBy.modelKey2",
                "replacedBy.replacedBy.modelDesc",
                "replacedBy.replacedBy.stationName",
                "replacedBy.replacedBy.modelMakeDesc",
                "replacedBy.replacedBy.modelMake",
                "replacedBy.replacedBy.modelMake2",
                "replacedBy.replacedBy.modelMakeKey",
                "replacedBy.replacedBy.modelMakeKey2",
                "replacedBy.replacedBy.modelMakeKey3",
                "replacedBy.replacedBy.modelMakeKey4",
                "replacedBy.replacedBy.modelMakeKey5",
                "replacedBy.replacedBy.modelMakeKey6",
                "replacedBy.replacedBy.modelMakeKey7",
                "replacedBy.replacedBy.modelMakeKey8",
                "replacedBy.replacedBy.make.key"
        ).isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly30() {
        select(TeVehicle.class).where().anyOfProps(
                "lastFuelUsage",
                "lastFuelUsageQty",
                "calc2",
                "calc3",
                "calc4",
                "replacedBy.calc2",
                "replacedBy.calc3",
                "replacedBy.calc4",
                "replacedBy.lastFuelUsage",
                "replacedBy.lastFuelUsageQty"
        ).isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly31() {
        select(TeVehicle.class).where().anyOfProps(
                "lastFuelUsageQty"
        ).isNotNull();
    }

//    @Test
//    public void eql3_query_executes_correctly32() {
//        select(TeVehicle.class).where().expr(
//                expr().model(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model()
//        ).isNotNull();
//    }
//
//    @Test
//    public void eql3_query_executes_correctly33() {
//        select(TeVehicle.class).where().expr(
//                expr().model(select(TeVehicleFuelUsage.class).as("fu").where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).and().prop("qty").isNotNull().yield().prop("fu.qty").modelAsPrimitive()).model()
//        ).isNotNull();
//    }
//
//    @Test
//    public void eql3_query_executes_correctly34() {
//        select(TgOrgUnit1.class).where().exists( //
//                select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id").and().exists( //
//                        select(TgOrgUnit3.class).where().prop("parent").eq().extProp("id").and().exists( //
//                                select(TgOrgUnit4.class).where().prop("parent").eq().extProp("id").and().exists( //
//                                        select(TgOrgUnit5.class).where().prop("parent").eq().extProp("id").and().prop("name").isNotNull(). //
//                                                model()). //
//                                        model()). //
//                                model()). //
//                        model()). //
//                and().prop("id").isNotNull();
//    }

    @Test
    public void eql3_query_executes_correctly35() {
        select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey6").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly36() {
        select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key").isNotNull();
    }

    @Test
    public void eql3_query_executes_correctly37() {
        select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key", "vehicle.replacedBy.modelMake2.key").isNotNull();
    }

}

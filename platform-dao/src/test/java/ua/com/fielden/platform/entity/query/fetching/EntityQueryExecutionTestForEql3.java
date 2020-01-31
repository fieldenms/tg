package ua.com.fielden.platform.entity.query.fetching;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class EntityQueryExecutionTestForEql3 extends AbstractDaoTestCase {
    private final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    private void run(final AggregatedResultQueryModel qry) {
        aggregateDao.getAllEntities(from(qry).with("EQL3", null).model());
    }

    private void run(final ICompoundCondition0<? extends AbstractEntity<?>> qryStart) {
        run(qryStart.yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").leftJoin(TeVehicle.class).as("rbv").
                on().prop("veh.replacedBy").eq().prop("rbv.id")./*or().prop("veh.replacedBy").ne().prop("rbv.id").*/
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("rbv.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("rbv.key").then().prop("veh.key").otherwise().prop("rbv.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        run(qry);
    }
    
    @Test
    public void eql3_query_executes_correctly2() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        
        run(qry);
    }
    
    @Test
    public void eql3_query_executes_correctly3() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        
        run(qry);
    }
    
    @Test
    public void eql3_query_executes_correctly4() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.key").as("vehicleKey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        
        final AggregatedResultQueryModel qry2 = select(qry).yield().prop("vehicleKey").as("vk").modelAsAggregate();
        
        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly5() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.replacedBy").as("vehicle").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        
        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle").isNotNull().yield().prop("vehicle.key").as("vk").modelAsAggregate();
        
        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly6() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.replacedBy").as("vehicle").
                modelAsAggregate();
        
        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle").isNotNull().yield().prop("vehicle.key").as("vk").modelAsAggregate();
        
        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly7() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.replacedBy").as("vehicle").
                modelAsAggregate();
        
        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle.station.name").isNull().yield().prop("vehicle").as("vk").modelAsAggregate();
        
        final AggregatedResultQueryModel qry3 = select(qry2).where().prop("vk.model").isNotNull().yield().prop("vk.model.make.key").as("makeKey").modelAsAggregate();
        
        run(qry3);
    }

    @Test
    public void eql3_query_executes_correctly8() {
        run(select(TeWorkOrder.class).where().prop("makeKey").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly9() {
        run(select(TeWorkOrder.class).where().prop("make.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly10() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly11() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey2").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly12() {
        run(select(TeWorkOrder.class).where().anyOfProps("model.makeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly13() {
        run(select(TeWorkOrder.class).where().anyOfProps("makeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly14() {
        run(select(TeVehicle.class).where().anyOfProps("calcModel").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly15() {
        run(select(TeVehicleMake.class).where().anyOfProps("c7").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly16() {
        run(select(TeVehicleMake.class).where().anyOfProps("c6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly17() {
        run(select(TeVehicleMake.class).where().anyOfProps("c3").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly18() {
        run(select(TeVehicleMake.class).where().anyOfProps("c8").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly19() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly20() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly21() {
        run(select(TeVehicle.class).where().anyOfProps("modelKey", "modelDesc").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly22() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey2", "make.key").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly23() {
        run(select(TeVehicle.class).where().anyOfProps("replacedBy.modelMakeKey2", "modelMakeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly24() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly25() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey3").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly26() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey4").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly27() {
        run(select(TeVehicle.class).where().anyOfProps(
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
                ).isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly28() {
        run(select(TeVehicle.class).where().anyOfProps(
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
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly29() {
        run(select(TeVehicle.class).where().anyOfProps(
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
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly30() {
        run(select(TeVehicle.class).where().anyOfProps(
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
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly31() {
        run(select(TeVehicle.class).where().anyOfProps(
                "lastFuelUsageQty"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly32() {
        run(select(TeVehicle.class).where().expr(
                expr().model(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model()
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly33() {
        run(select(TeVehicle.class).where().expr(
                expr().model(select(TeVehicleFuelUsage.class).as("fu").where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).and().prop("qty").isNotNull().yield().prop("fu.qty").modelAsPrimitive()).model()
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly34() {
        run(select(TgOrgUnit1.class).where().exists( //
                select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit3.class).where().prop("parent").eq().extProp("id").and().exists( // 
                        select(TgOrgUnit4.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit5.class).where().prop("parent").eq().extProp("id").and().prop("name").isNotNull(). //
                        model()). //
                        model()). //
                        model()). //
                        model()). //
                and().prop("id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly35() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly36() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly37() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key", "vehicle.replacedBy.modelMake2.key").isNotNull());
    }
    
    @Test
    @Ignore
    public void eql3_query_executes_correctly38() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).where().prop("model.make.key").isNotNull().model();
        
        run(select(qry).where().prop("id").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly39() {
        final AggregatedResultQueryModel qry = select(TeWorkOrder.class).yield().prop("vehicle").as("vh").modelAsAggregate();
        
        run(select(qry).where().prop("vh.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly40() {
        final AggregatedResultQueryModel qry1 = select(TeWorkOrder.class).where().prop("actCost").isNotNull().yield().prop("vehicle").as("vh").modelAsAggregate();
        final AggregatedResultQueryModel qry2 = select(TeWorkOrder.class).where().prop("estCost").isNotNull().yield().prop("vehicle").as("vh").modelAsAggregate();
        
        run(select(qry1, qry2).where().prop("vh.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    @Ignore
    public void eql3_query_executes_correctly41() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeWorkOrder.class).yield().prop("vehicle").modelAsEntity(TeVehicle.class);
        
        run(select(qry).where().prop("lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly42() {
        run(select(TeWorkOrder.class).where().prop("vehicle.modelMakeKey6").eq().prop("makeKey"));
    }

    @Test
    public void eql3_query_executes_correctly43() {
        run(select(TeWorkOrder.class).where().prop("vehicle.modelMakeKey6").eq().iVal(null));
    }
    
    @Test
    public void eql3_query_executes_correctly44() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).yield().prop("id").as("vehicle").modelAsAggregate();
        
        run(select(qry).where().prop("vehicle.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

}
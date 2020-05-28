package ua.com.fielden.platform.entity.query.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITeAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.ITeVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgEntityWithComplexSummaries;
import ua.com.fielden.platform.sample.domain.TeAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgEntityWithComplexSummaries;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

public class EntityQuery3ExecutionTest extends AbstractDaoTestCase {
    private final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    private List<EntityAggregates> run(final AggregatedResultQueryModel qry) {
        return aggregateDao.getAllEntities(from(qry).with("EQL3", null).model());
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
    public void eql3_query_executes_correctly38() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).where().prop("model.make.key").isNotNull().model();
        
        run(select(qry).where().anyOfProps("id", "modelMake").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
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

    @Test
    public void eql3_query_executes_correctly45() {
        run(select(TeVehicleFuelUsage.class).where().prop("key").isNull().or().prop("key").eq().val("HOH").or().prop("qty").gt().val(0).or().prop("vehicle.active").eq().val(true));
    }
    
    @Test
    public void eql3_query_executes_correctly46() {
        run(select(TeVehicleFuelUsage.class).where().exists(select(TeVehicle.class).where().prop("key").eq().val("A101").model()));
    }

    @Test
    public void eql3_query_executes_correctly47() {
        final AggregatedResultQueryModel qry1 = select().yield().val(1).as("position").yield().val(100).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry2 = select().yield().val(2).as("position").yield().val(200).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry3 = select().yield().val(3).as("position").yield().val(300).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry4 = select().yield().val(4).as("position").yield().val(400).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry5 = select().yield().val(5).as("position").yield().val(500).as("value").modelAsAggregate();
        
        final List<EntityAggregates> result = run(select(qry1, qry2, qry3, qry4, qry5).where().prop("position").gt().val(2).yield().sumOf().prop("value").as("QTY").modelAsAggregate());
        assertEquals("1200", result.get(0).get("QTY").toString());
    }
    
    @Test
    public void eql3_query_executes_correctly48() {
        run(select(TeVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100));
    }
    
    @Test
    public void eql3_query_executes_correctly49() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        final List<TeVehicleModel> models = getInstance(ITeVehicleModel.class).getAllEntities(from(qry).with(fetchAll(TeVehicleModel.class).with("makeKey")).with("EQL3", null).model());
        for (final TeVehicleModel item : models) {
            System.out.println(item.getId() + " : " + item.getKey() + " : " + item.getMake() + " : " + item.getMakeKey());
        }
    }

    @Test
    public void eql3_query_executes_correctly50() {
        run(select(TeVehicleModel.class).where().prop("make.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly51() {
        run(select(TeVehicle.class).where().prop("model.make.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly52() {
        run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage1").on().prop("lastFuelUsage").eq().prop("lastFuelUsage1.id").where().prop("lastFuelUsage.qty").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly53() {
        run(select(TeVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100));
    }
    
    @Test
    public void eql3_query_executes_correctly54() {
        run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage1").on().prop("lastFuelUsage").eq().prop("lastFuelUsage1.id").where().prop("lastFuelUsage1.qty").gt().val(100));
    }
    
    @Test
    public void eql3_query_executes_correctly55() {
        try {
            run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage").on().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").where().prop("lastFuelUsage.qty").gt().val(100));
            fail("Should have failed while trying to resolve property [lastFuelUsage.qty]");
        } catch (final Exception e) {
        }
    }
    
    @Test
    @Ignore // h2 doesn't allow correlated subqueries in FROM stmt
    public void eql3_query_executes_correctly56() {

        final PrimitiveResultQueryModel qtyQry = select(select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate()).yield().prop("qty").modelAsPrimitive();
        //final PrimitiveResultQueryModel qtyQry = select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().modelAsPrimitive();
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().prop("key").as("key").yield().model(qtyQry).as("qty").modelAsAggregate();

        run(qry);
    }
    
    @Test
    public void eql3_query_executes_correctly57() {
        run(select(TeAverageFuelUsage.class).where().prop("key.modelMakeKey6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly58() {
        final EntityResultQueryModel<TeAverageFuelUsage> qry = select(TeAverageFuelUsage.class).model();
        final List<TeAverageFuelUsage> models = getInstance(ITeAverageFuelUsage.class).getAllEntities(from(qry).with(fetchAll(TeAverageFuelUsage.class)).with("EQL3", null).model());
        for (final TeAverageFuelUsage item : models) {
            System.out.println(item.getId() + " : " + item.getKey() + " : " + item.getQty());
        }
    }
    
    @Test
    public void eql3_query_executes_correctly59() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TeVehicleModel.class).with("makeKey2")).model());
    }

    @Test
    public void eql3_query_executes_correctly60() {
        final ITgEntityWithComplexSummaries co = getInstance(ITgEntityWithComplexSummaries.class);
        final EntityResultQueryModel<TgEntityWithComplexSummaries> qry = select(TgEntityWithComplexSummaries.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).model());
    }
    
    @Test
    public void eql3_query_executes_correctly61() {
        final ITgEntityWithComplexSummaries co = getInstance(ITgEntityWithComplexSummaries.class);
        final EntityResultQueryModel<TgEntityWithComplexSummaries> qry = select(TgEntityWithComplexSummaries.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchIdOnly(TgEntityWithComplexSummaries.class).without("id").with("costPerKm")).model());
    }
    
    @Test
    public void eql3_query_executes_correctly62() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        qry.setFilterable(true);
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TeVehicleModel.class).with("makeKey2")).model());
    }
    
    @Test
    public void eql3_query_executes_correctly63() {
        final List<EntityAggregates> result = run(select(TgVehicle.class).where().prop("key").eq().val("CAR1").yield().prop("active").as("active").modelAsAggregate());
        assertEquals(true, result.get(0).get("active"));
    }
    
    @Test
    public void eql3_query_executes_correctly64() {
        run(select(TgVehicle.class).where().prop("finDetails.capitalWorksNo").isNotNull());
    }
    
    @Test
    public void eql3_query_executes_correctly65() {
        run(select(TgBogie.class).where().anyOfProps("location.workshop.key", "location.wagonSlot.wagon", "location.wagonSlot.key").isNotNull());
    }

    @Test
    @Ignore
    public void eql3_query_executes_correctly66() {
        run(select(TgBogie.class).where().anyOfProps("location.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly67() {
        final ITgBogie co = getInstance(ITgBogie.class);
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
    }
    
    @Test
    public void eql3_query_executes_correctly68() {
        final ITgBogie co = getInstance(ITgBogie.class);
        final EntityResultQueryModel<TgBogie> qry = select(select(TgBogie.class).model()).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));

        final TgBogieLocation location = co$(TgBogieLocation.class).new_();
        location.setWorkshop(workshop1);
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2"));
        final TgBogie bogie3 = save(new_(TgBogie.class, "BOGIE3", "Bogie 3"));
        final TgBogie bogie4 = save(new_(TgBogie.class, "BOGIE4", "Bogie 4"));
        final TgBogie bogie5 = save(new_(TgBogie.class, "BOGIE5", "Bogie 5"));
        final TgBogie bogie6 = save(new_(TgBogie.class, "BOGIE6", "Bogie 6"));
        final TgBogie bogie7 = save(new_(TgBogie.class, "BOGIE7", "Bogie 7"));
        
        final TgWagon wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final TgWagon wagon2 = save(new_(TgWagon.class, "WAGON2", "Wagon 2"));

        save(new_composite(TgWagonSlot.class, wagon1, 5));
        save(new_composite(TgWagonSlot.class, wagon1, 6));
        save(new_composite(TgWagonSlot.class, wagon1, 7));
        save(new_composite(TgWagonSlot.class, wagon1, 8));
        save(new_composite(TgWagonSlot.class, wagon1, 4).setBogie(bogie1));
        save(new_composite(TgWagonSlot.class, wagon1, 3).setBogie(bogie2));
        save(new_composite(TgWagonSlot.class, wagon1, 2).setBogie(bogie3));
        save(new_composite(TgWagonSlot.class, wagon1, 1).setBogie(bogie4));

        save(new_composite(TgWagonSlot.class, wagon2, 1).setBogie(bogie5));
        save(new_composite(TgWagonSlot.class, wagon2, 2).setBogie(bogie6));
        
        final TgWagonSlot wagonSlot = save(new_composite(TgWagonSlot.class, wagon2, 3).setBogie(bogie7).setFuelType(petrolFuelType));
        final TgBogieLocation slotLocation = co$(TgBogieLocation.class).new_();
        slotLocation.setWagonSlot(wagonSlot);
        save(bogie7.setLocation(slotLocation));
        
        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5"));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
        final TgVehicleMake subaro = save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TeVehicleMake merc_ = save(new_(TeVehicleMake.class, "MERC", "Mercedes"));
        final TeVehicleMake audi_ = save(new_(TeVehicleMake.class, "AUDI", "Audi"));
        final TeVehicleMake bmw_ = save(new_(TeVehicleMake.class, "BMW", "BMW"));
        final TeVehicleMake subaro_ = save(new_(TeVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final TgVehicleModel m317 = save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        final TgVehicleModel m319 = save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        final TgVehicleModel m320 = save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        final TgVehicleModel m321 = save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        final TgVehicleModel m322 = save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TeVehicleModel m316_ = save(new_(TeVehicleModel.class, "316", "316").setMake(merc_));
        final TeVehicleModel m317_ = save(new_(TeVehicleModel.class, "317", "317").setMake(audi_));
        final TeVehicleModel m318_ = save(new_(TeVehicleModel.class, "318", "318").setMake(audi_));
        final TeVehicleModel m319_ = save(new_(TeVehicleModel.class, "319", "319").setMake(bmw_));
        final TeVehicleModel m320_ = save(new_(TeVehicleModel.class, "320", "320").setMake(bmw_));
        final TeVehicleModel m321_ = save(new_(TeVehicleModel.class, "321", "321").setMake(bmw_));
        final TeVehicleModel m322_ = save(new_(TeVehicleModel.class, "322", "322").setMake(bmw_));

        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5).setReplacedBy(car1));

        save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

        final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
        final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
        final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
        final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
        final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
        final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

        final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true));
        final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1));

        save(new_composite(UserAndRoleAssociation.class, user1, managerRole));
        save(new_composite(UserAndRoleAssociation.class, user1, analyticRole));
        save(new_composite(UserAndRoleAssociation.class, user2, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user2, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user2, warehouseOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user3, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, warehouseOperatorRole));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor chrisDate = save(new_composite(TgAuthor.class, chris, "Date", null));

        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        final TgAuthor yurijShcherbyna = save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych"));

        save(new_composite(TgAuthorship.class, chrisDate, "An Introduction to Database Systems").setYear(2003));
        save(new_composite(TgAuthorship.class, chrisDate, "Database Design and Relational Theory").setYear(2012));
        save(new_composite(TgAuthorship.class, chrisDate, "SQL and Relational Theory").setYear(2015));
        save(new_composite(TgAuthorship.class, yurijShcherbyna, "Дискретна математика").setYear(2007));

        save(new_(TgEntityWithComplexSummaries.class, "veh1").setKms(200).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh2").setKms(0).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh3").setKms(300).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh4").setKms(0).setCost(200));
    }
}